package com.docflow.model;

public class DocumentVersion {

    private int versionNumber;
    private String content;

    public DocumentVersion(int versionNumber, String content) {
        this.versionNumber = versionNumber;
        this.content = content;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getContent() {
        return content;
    }
}
