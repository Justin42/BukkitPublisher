package me.b2ojustin.bukkit.bukkitclient;

import java.io.File;
import java.util.ArrayList;

public class FileUploadDescriptor {
    private File file;

    public String getFileVersion() {
        return fileVersion;
    }

    public FileUploadDescriptor setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
        return this;
    }

    private String fileVersion = "";
    private String bukkitVersion = "";
    private String name = "";
    private ArrayList<String> changes = new ArrayList<>();
    private ArrayList<String> caveats = new ArrayList<>();
    private ReleaseType releaseType = ReleaseType.ALPHA;
    private String projectUrl;


    public FileUploadDescriptor(File file) {
        this.file = file;
    }

    public FileUploadDescriptor() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Project URL: %s \n", projectUrl));
        sb.append(String.format("File Path: %s \n", file.getPath()));
        sb.append(String.format("Name: %s \n", name));
        sb.append(String.format("Release Type: %s \n", releaseType.toString()));
        sb.append(String.format("Bukkit Version: %s \n\n", bukkitVersion));

        sb.append(String.format("Changes - \n"));
        for(String change : changes) {
            sb.append(change).append("\n");
        }
        sb.append("\n");

        sb.append("Known Caveats - \n");
        for(String caveat : caveats) {
            sb.append(caveat).append("\n");
        }
        return sb.toString();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public FileUploadDescriptor setBukkitVersion(String version) {
        this.bukkitVersion = version;
        return this;
    }

    public String getBukkitVersion() {
        return bukkitVersion;
    }

    public FileUploadDescriptor setName(String version) {
        this.name = version;
        return this;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getChanges() {
        return changes;
    }

    public FileUploadDescriptor setChanges(ArrayList<String> changes) {
        this.changes = changes;
        return this;
    }

    public ArrayList<String> getCaveats() {
        return caveats;
    }

    public FileUploadDescriptor setCaveats(ArrayList<String> caveats) {
        this.caveats = caveats;
        return this;
    }

    public ReleaseType getReleaseType() {
        return releaseType;
    }

    public FileUploadDescriptor setReleaseType(ReleaseType releaseType) {
        this.releaseType = releaseType;
        return this;
    }

    public FileUploadDescriptor setProjectUrl(String url) {
        this.projectUrl = url;
        return this;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

}
