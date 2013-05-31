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
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import me.b2ojustin.bukkit.bukkitclient.BukkitClient;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.File;
import java.io.IOException;


@SuppressWarnings("UnusedDeclaration")
public class BukkitPublisher extends Notifier {
    public String apiKey;
    public boolean updatePages;
    public String projectUrl;
    public String fileName;

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
    public BukkitPublisher(String apiKey, String projectName, String fileName, boolean updatePages) {
        this.apiKey = apiKey;
        this.projectUrl = projectName;
        this.fileName = fileName;
        this.updatePages = updatePages;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if(!build.getResult().isBetterOrEqualTo(Result.SUCCESS) || !build.getHasArtifacts()) return false;
        BukkitClient bClient = new BukkitClient(projectUrl, apiKey);

        // Grab the artifact.
        if(!fileName.isEmpty()) {
            File artifactFile = null;
            for(Run<? extends AbstractProject<?, ?>, ? extends AbstractBuild<?, ?>>.Artifact artifact : build.getArtifacts()) {
                if(artifact.getFileName().matches(fileName)) {
                    artifactFile = artifact.getFile();
                    break;
                }
            }
            if(artifactFile == null) return false;

            // Get module path.
            for(FilePath path : build.getModuleRoots()) {

            }
        }



        return true;
    }
}

