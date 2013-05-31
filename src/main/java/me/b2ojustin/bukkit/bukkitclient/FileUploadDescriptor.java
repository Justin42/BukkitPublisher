package me.b2ojustin.bukkit.bukkitclient;

import java.io.File;
import java.util.ArrayList;

public class FileUploadDescriptor {
    private File file;
    private String bukkitVersion = "";
    private String fileVersion = "";
    private ArrayList<String> changes = new ArrayList<>();
    private ArrayList<String> caveAts = new ArrayList<>();
    private ReleaseType releaseType = ReleaseType.ALPHA;
    private String projectUrl;


    public FileUploadDescriptor(File file) {
        this.file = file;
    }

    public FileUploadDescriptor setBukkitVersion(String version) {
        this.bukkitVersion = version;
        return this;
    }

    public String getBukkitVersion() {
        return bukkitVersion;
    }

    public FileUploadDescriptor setFileVersion(String version) {
        this.fileVersion = version;
        return this;
    }

    public String getFileVersion() {
        return fileVersion;
    }

    public ArrayList<String> getChanges() {
        return changes;
    }

    public FileUploadDescriptor setChanges(ArrayList<String> changes) {
        this.changes = changes;
        return this;
    }

    public ArrayList<String> getCaveAts() {
        return caveAts;
    }

    public FileUploadDescriptor setCaveAts(ArrayList<String> caveAts) {
        this.caveAts = caveAts;
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
