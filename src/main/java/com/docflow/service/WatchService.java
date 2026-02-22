package com.docflow.service;

import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WatchService {

    private final DocumentRepository documentRepository;

    public WatchService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public void follow(User user, String documentId) {
        Objects.requireNonNull(user, "User cannot be null");
        Document document = getAccessibleDocument(user, documentId);
        user.followDocument(document.getId());
        user.markDocumentVersionSeen(document.getId(), document.getVersion());
    }

    public void unfollow(User user, String documentId) {
        Objects.requireNonNull(user, "User cannot be null");
        if (documentId == null || documentId.isBlank()) {
            return;
        }
        user.unfollowDocument(documentId);
    }

    public List<Document> listFollowed(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        List<Document> result = new ArrayList<>();
        for (String documentId : user.getFollowedDocuments()) {
            documentRepository.findById(documentId).ifPresent(result::add);
        }
        return result;
    }

    public List<Document> listUpdatedSinceLastSeen(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        List<Document> result = new ArrayList<>();
        for (String documentId : user.getFollowedDocuments()) {
            Optional<Document> doc = documentRepository.findById(documentId);
            if (doc.isEmpty()) {
                continue;
            }
            Document document = doc.get();
            int lastSeen = user.getLastSeenVersion(documentId);
            if (document.getVersion() > lastSeen) {
                result.add(document);
            }
        }
        return result;
    }

    public void markAllSeen(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        for (String documentId : user.getFollowedDocuments()) {
            documentRepository.findById(documentId)
                    .ifPresent(doc -> user.markDocumentVersionSeen(documentId, doc.getVersion()));
        }
    }

    private Document getAccessibleDocument(User user, String documentId) {
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
        return document;
    }
}
