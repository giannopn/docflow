package com.docflow.model;

public class Document {

    private int id;
    private String title;
    private String author;
    private String category;
    private String createdAt;
    private int version;
    private String content;

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
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return title + " (" + author + ")";
    }
}

