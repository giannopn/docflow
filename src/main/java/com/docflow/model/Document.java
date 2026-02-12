package com.docflow.model;

import java.time.LocalDate;

public class Document {

    private int id;
    private String title;
    private String author;
    private String category;
    private String createdAt;
    private int version;
    private String content;
    private boolean deleted;

    public Document(int id, String title, String author,
                    String category, String createdAt,
                    int version, String content) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.createdAt = createdAt;
        this.version = version;
        this.content = content;
        this.deleted = false;
    }

    public Document(String title,
                    String author,
                    String category,
                    LocalDate createdAt,
                    String content) {
        this(0, title, author, category, createdAt.toString(), 1, content);
    }

    public int getId() {
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

    public String getCreatedAt() {
        return createdAt;
    }

    public int getVersion() {
        return version;
    }

    public String getContent() {
        return content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.version++;
    }

    public void markDeleted() {
        this.deleted = true;
    }

    @Override
    public String toString() {
        return title + " (" + author + ")";
    }
}
