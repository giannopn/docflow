package com.docflow.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Document {

    private String id;
    private String title;
    private String author;
    private String category;
    private String createdAt;
    private List<DocumentVersion> versions;

    public Document(String id, String title, String author,
                    String category, String createdAt,
                    List<DocumentVersion> versions) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.createdAt = createdAt;
        this.versions = new ArrayList<>(Objects.requireNonNull(versions, "Versions cannot be null"));
        if (this.versions.isEmpty()) {
            throw new IllegalArgumentException("Document must contain at least one version");
        }
    }

    public Document(int id, String title, String author,
                    String category, String createdAt,
                    List<DocumentVersion> versions) {
        this(String.valueOf(id), title, author, category, createdAt, versions);
    }

    public Document(String title,
                    String author,
                    String category,
                    LocalDate createdAt,
                    String content) {
        this(
                UUID.randomUUID().toString(),
                title,
                author,
                category,
                createdAt.toString(),
                List.of(new DocumentVersion(1, content))
        );
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
        return latestVersion().getVersionNumber();
    }

    public String getContent() {
        return latestVersion().getContent();
    }

    public List<DocumentVersion> getVersions() {
        return new ArrayList<>(versions);
    }

    public void updateContent(String newContent) {
        int newVersionNumber = latestVersion().getVersionNumber() + 1;
        versions.add(new DocumentVersion(newVersionNumber, newContent));
    }

    private DocumentVersion latestVersion() {
        if (versions == null || versions.isEmpty()) {
            throw new IllegalStateException("Document must contain at least one version");
        }
        return versions.get(versions.size() - 1);
    }

    @Override
    public String toString() {
        return title + " (" + author + ")";
    }
}
