package com.docflow.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Document {

    private String id;
    private String title;
    private String author;
    private String category;
    private String createdAt;
    // Legacy fields kept for JSON compatibility in this phase.
    private int version;
    private String content;
    private List<DocumentVersion> versions;

    public Document(String id, String title, String author,
                    String category, String createdAt,
                    int version, String content) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.createdAt = createdAt;
        this.version = version;
        this.content = content;
        this.versions = new ArrayList<>();
        this.versions.add(new DocumentVersion(version, content));
    }

    public Document(int id, String title, String author,
                    String category, String createdAt,
                    int version, String content) {
        this(String.valueOf(id), title, author, category, createdAt, version, content);
    }

    public Document(String title,
                    String author,
                    String category,
                    LocalDate createdAt,
                    String content) {
        this(UUID.randomUUID().toString(), title, author, category, createdAt.toString(), 1, content);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getVersion() {
        ensureVersionsInitialized();
        return versions.get(versions.size() - 1).getVersionNumber();
    }

    public String getContent() {
        ensureVersionsInitialized();
        return versions.get(versions.size() - 1).getContent();
    }

    public List<DocumentVersion> getVersions() {
        ensureVersionsInitialized();
        return new ArrayList<>(versions);
    }

    public void updateContent(String newContent) {
        ensureVersionsInitialized();
        int newVersionNumber = versions.get(versions.size() - 1).getVersionNumber() + 1;
        versions.add(new DocumentVersion(newVersionNumber, newContent));

        // Keep legacy fields in sync until persistence is redesigned.
        this.version = newVersionNumber;
        this.content = newContent;
    }

    private void ensureVersionsInitialized() {
        if (versions == null) {
            versions = new ArrayList<>();
        }
        if (versions.isEmpty()) {
            versions.add(new DocumentVersion(version, content));
        }
    }

    @Override
    public String toString() {
        return title + " (" + author + ")";
    }
}
