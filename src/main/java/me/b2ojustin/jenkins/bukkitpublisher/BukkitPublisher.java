/*
 * This file is part of BukkitPublisher.
 *
 *     BukkitPublisher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     BukkitPublisher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with BukkitPublisher.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.b2ojustin.jenkins.bukkitpublisher;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import me.b2ojustin.bukkit.bukkitclient.BukkitClient;
import me.b2ojustin.bukkit.bukkitclient.FileUploadDescriptor;
import me.b2ojustin.bukkit.bukkitclient.ReleaseType;
import net.sf.json.JSONObject;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;


@SuppressWarnings("UnusedDeclaration")
public class BukkitPublisher extends Notifier {
    private final static Logger logger = Logger.getLogger(BukkitPublisher.class.getName());
    public String apiKey;
    public boolean useCustomApiKey;
    public boolean updatePages;
    public boolean failBuild;
    public String projectUrl;
    public String fileName;
    public String releaseType;

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    // Plugin descriptor
    @Extension
    public final static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Exported
        private String globalApiKey;

        @Exported
        private String username;

        public DescriptorImpl() {
            super(BukkitPublisher.class);
            load();
        }

        public String getDisplayName() {
            return "Publish to Bukkit";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
            globalApiKey = json.getString("globalApiKey");
            username = json.getString("username");
            save();
            return true;
        }

        public String getGlobalApiKey() {
            return globalApiKey;
        }

        public String getUsername() {
            return username;
        }
    }

    @DataBoundConstructor
    public BukkitPublisher(String apiKey, String projectUrl, String fileName, boolean updatePages, String releaseType, boolean useCustomApiKey, boolean passBuild) {
        this.apiKey = apiKey;
        this.projectUrl = projectUrl;
        this.fileName = fileName;
        this.updatePages = updatePages;
        this.useCustomApiKey = useCustomApiKey;
        this.releaseType = releaseType;
        this.failBuild = passBuild;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        logger.info("Publishing to bukkit...");
        if(!build.getResult().isBetterOrEqualTo(Result.SUCCESS)) {
            logger.info("Build was unsuccessful, not publishing...");
            return !failBuild;
        }

        if(!build.getHasArtifacts()) {
            logger.info("Build has no artifacts, nothing to publish.");
            return !failBuild;
        }

        if(fileName.isEmpty()) {
            logger.info("No filename specified for project.");
            return !failBuild;
        }
        if(projectUrl.isEmpty()) {
            logger.info("No project URL specified for project.");
            return !failBuild;
        }

        final FileUploadDescriptor uploadDescriptor = new FileUploadDescriptor();
        uploadDescriptor.setReleaseType(ReleaseType.valueOf(this.releaseType.toUpperCase()));

        String name = null;
        WildcardFileFilter filter = new WildcardFileFilter(fileName);
        for(Run<? extends AbstractProject<?, ?>, ? extends AbstractBuild<?, ?>>.Artifact artifact : build.getArtifacts()) {
            logger.info("Found artifact. " + artifact.getFileName());
            if(filter.accept(artifact.getFile())) {
                uploadDescriptor.setFile(artifact.getFile());
                break;
            }
        }
        if(uploadDescriptor.getFile() == null) {
            logger.info("No artifacts match filename");
            return !failBuild;
        }

        // Get module directories.
        ArrayList<FilePath> moduleRoots = new ArrayList<>();
        for(FilePath file : build.getModuleRoot().list()) {
            if(file.getName().equals("pom.xml")) {
                moduleRoots.add(build.getModuleRoot());
                break;
            }
        }
        for(FilePath path : build.getModuleRoot().listDirectories()) {
            for(FilePath file : path.list()) {
                if(file.getName().equals("pom.xml")) {
                    moduleRoots.add(path);
                    break;
                }
            }
        }

        // Get the bukkit version from the pom file
        for(final FilePath path : moduleRoots) {
            boolean success = path.act(new FilePath.FileCallable<Boolean>(){
                @Override
                public Boolean invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

                    // Make sure we have the correct module.
                    if(f.isDirectory()) {
                        File file = new File(f.getAbsolutePath() + "/target/" + uploadDescriptor.getFile().getName());
                        if(!file.exists()) {
                            //logger.info("Could not find artifact file at " + file.getAbsolutePath());
                            return false;
                        }
                    }
                    else return false;

                    // Load the pom file
                    File pomFile = new File(f.getAbsolutePath() + "/pom.xml");
                    if(!pomFile.exists()) {
                        //logger.info("Couldn't retrieve pom file at " + pomFile.getAbsolutePath());
                        return false;
                    }

                    // Get bukkit version
                    try {
                        MavenXpp3Reader mReader = new MavenXpp3Reader();
                        Model model = mReader.read(new FileReader(pomFile));

                        // Get bukkit version
                        for(Dependency dependency : model.getDependencies()) {
                            String artifactId = dependency.getArtifactId();
                            if(artifactId.equals("bukkit") || artifactId.equals("craftbukkit")) {
                                uploadDescriptor.setBukkitVersion(dependency.getVersion());
                            }
                        }
                        if(uploadDescriptor.getBukkitVersion() == null) return false;

                        // Get file version
                        if(model.getVersion() == null) return false;
                        String version = model.getVersion();
                        if(version.endsWith("-SNAPSHOT")) version = version.replace("-SNAPSHOT", "");
                        uploadDescriptor.setFileVersion(version);

                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });
            if(success) break;
        }

        // Set name
        switch(uploadDescriptor.getReleaseType()) {
            case BETA:
            case ALPHA:
                name = build.getProject().getDisplayName() + uploadDescriptor.getFileVersion() + "-b" + build.getNumber();
                break;
            case RELEASE:
                name = build.getProject().getDisplayName() + uploadDescriptor.getFileVersion();
                break;
        }
        uploadDescriptor.setName(name);


        // Get changes
        ArrayList<String> changes = new ArrayList<>();
        for(ChangeLogSet.Entry changeEntry : build.getChangeSet()) {
            changes.add(changeEntry.getMsg());
        }
        uploadDescriptor.setChanges(changes);

        // Get API key
        String apiKey;
        if(this.apiKey.isEmpty() || !useCustomApiKey) {
            apiKey = getDescriptor().getGlobalApiKey();
        } else apiKey = this.apiKey;

        // Upload file
        BukkitClient bClient = new BukkitClient(apiKey);
        //bClient.uploadFile(fDescriptor);

        logger.info(uploadDescriptor.toString());
        return true;
    }
}

