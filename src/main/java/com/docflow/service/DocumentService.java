package com.docflow.service;

import com.docflow.model.Document;
import com.docflow.model.DocumentVersion;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public List<Document> listAccessible(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        List<Document> result = new ArrayList<>();
        for (Document document : documentRepository.findAll()) {
            if (document.isDeleted()) {
                continue;
            }
            if (user.hasAccessToCategory(document.getCategory())) {
                result.add(document);
            }
        }
        return result;
    }

    public Optional<Document> findById(User user, String documentId) {
        Objects.requireNonNull(user, "User cannot be null");
        if (documentId == null || documentId.isBlank()) {
            return Optional.empty();
        }

        Optional<Document> doc = documentRepository.findById(documentId);
        if (doc.isEmpty()) {
            return Optional.empty();
        }
        Document document = doc.get();
        if (document.isDeleted()) {
            return Optional.empty();
        }
        if (!user.hasAccessToCategory(document.getCategory())) {
            return Optional.empty();
        }
        return Optional.of(document);
    }

    public List<Document> search(User user, String title, String author, String category) {
        Objects.requireNonNull(user, "User cannot be null");
        String titleQuery = normalize(title);
        String authorQuery = normalize(author);
        String categoryQuery = normalize(category);

        List<Document> result = new ArrayList<>();
        for (Document document : listAccessible(user)) {
            if (!titleQuery.isEmpty() && !document.getTitle().toLowerCase().contains(titleQuery)) {
                continue;
            }
            if (!authorQuery.isEmpty() && !document.getAuthor().toLowerCase().contains(authorQuery)) {
                continue;
            }
            if (!categoryQuery.isEmpty() && !document.getCategory().toLowerCase().contains(categoryQuery)) {
                continue;
            }
            result.add(document);
        }
        return result;
    }

    public Document create(User user, String title, String category, String content) {
        requireCanManageDocuments(user);
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (!user.hasAccessToCategory(category)) {
            throw new IllegalStateException("User has no access to this category");
        }

        Document document = new Document(title, user.getFullName(), category, LocalDate.now(), content);
        documentRepository.add(document);
        return document;
    }

    public void updateContent(User user, String documentId, String newContent) {
        requireCanManageDocuments(user);
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("Document id cannot be empty");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (document.isDeleted()) {
            throw new IllegalStateException("Document is deleted");
        }
        if (!user.hasAccessToCategory(document.getCategory())) {
            throw new IllegalStateException("User has no access to this category");
        }

        document.updateContent(newContent);
        documentRepository.update(document);
    }

    public boolean delete(User user, String documentId) {
        requireCanManageDocuments(user);
        if (documentId == null || documentId.isBlank()) {
            return false;
        }
        Optional<Document> doc = documentRepository.findById(documentId);
        if (doc.isEmpty()) {
            return false;
        }
        Document document = doc.get();
        if (document.isDeleted()) {
            throw new IllegalStateException("Document is deleted");
        }
        if (!user.hasAccessToCategory(document.getCategory())) {
            throw new IllegalStateException("User has no access to this category");
        }
        return documentRepository.remove(documentId);
    }

    public List<DocumentVersion> getVisibleVersions(User user, Document document) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(document, "Document cannot be null");

        List<DocumentVersion> versions = document.getVersions();
        if (!user.canManageDocuments()) {
            return versions.isEmpty() ? List.of() : List.of(versions.get(versions.size() - 1));
        }

        int fromIndex = Math.max(0, versions.size() - 3);
        return new ArrayList<>(versions.subList(fromIndex, versions.size()));
    }

    private void requireCanManageDocuments(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        if (!user.canManageDocuments()) {
            throw new IllegalStateException("User cannot manage documents");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
