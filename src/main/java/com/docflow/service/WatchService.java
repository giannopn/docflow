package com.docflow.service;

import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages document follow/unfollow behavior and version-based update notifications.
 */
public class WatchService {

    private final DocumentRepository documentRepository;

    /**
     * Creates a watch service backed by the document repository.
     *
     * @param documentRepository repository used to resolve followed documents
     */
    public WatchService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /**
     * Starts following a document that the user can access.
     * The current document version is marked as seen at follow time.
     *
     * @param user the user that follows the document
     * @param documentId the target document id
     */
    public void follow(User user, String documentId) {
        Objects.requireNonNull(user, "User cannot be null");
        Document document = getAccessibleDocument(user, documentId);
        user.followDocument(document.getId());
        user.markDocumentVersionSeen(document.getId(), document.getVersion());
    }

    /**
     * Stops following a document for the given user.
     *
     * @param user the user that unfollows the document
     * @param documentId the target document id
     */
    public void unfollow(User user, String documentId) {
        Objects.requireNonNull(user, "User cannot be null");
        if (documentId == null || documentId.isBlank()) {
            return;
        }
        user.unfollowDocument(documentId);
    }

    /**
     * Lists documents currently followed by the given user.
     *
     * @param user the user whose watch list is returned
     * @return followed documents that still exist in storage
     */
    public List<Document> listFollowed(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        List<Document> result = new ArrayList<>();
        for (String documentId : user.getFollowedDocuments()) {
            documentRepository.findById(documentId).ifPresent(result::add);
        }
        return result;
    }

    /**
     * Lists followed documents that have a newer version than the user's last seen version.
     *
     * @param user the user whose update notifications are checked
     * @return followed documents with unseen new versions
     */
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

    /**
     * Marks all currently followed documents as seen at their latest versions.
     *
     * @param user the user whose followed documents are marked as seen
     */
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
        if (!user.hasAccessToCategory(document.getCategory())) {
            throw new IllegalStateException("User has no access to this category");
        }
        return document;
    }
}
