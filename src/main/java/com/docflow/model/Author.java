package com.docflow.model;

import java.time.LocalDate;
import java.util.Set;

/**
 * Author user.
 * Can create, edit and delete documents in allowed categories.
 */
public class Author extends SimpleUser {

    public Author(String firstName,
                  String lastName,
                  String username,
                  String password,
                  Set<String> allowedCategories) {
        super(firstName, lastName, username, password, allowedCategories);
    }

    /**
     * Creates a new document with initial version = 1.
     */
    public Document createDocument(String title,
                                   String category,
                                   String content) {

        if (!hasAccessToCategory(category)) {
            throw new IllegalStateException("User has no access to this category");
        }

        return new Document(
                title,
                getFullName(),
                category,
                LocalDate.now(),
                content
        );
    }

    /**
     * Updates the content of a document, creating a new version.
     */
    public void editDocument(Document document, String newContent) {
        document.updateContent(newContent);
    }

    /**
     * Deletes a document.
     * Actual removal is handled by the repository.
     */
    public void deleteDocument(Document document) {
        document.markDeleted();
    }

    @Override
    public UserRole getRole() {
        return UserRole.AUTHOR;
    }
}
