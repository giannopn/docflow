package com.docflow.repository;

import com.docflow.model.Author;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldBootstrapDefaultAdminWhenUsersFileMissing() throws IOException {
        Path dataDir = tempDir.resolve("medialab");
        UserRepository userRepository = new UserRepository(dataDir);

        userRepository.load();

        User admin = userRepository.findByUsername("medialab").orElseThrow();
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(admin.checkPassword("medialab_2025"));
    }

    @Test
    void shouldPersistAndReloadUsersDocumentsAndCategories() throws IOException {
        Path dataDir = tempDir.resolve("medialab");

        UserRepository userRepository = new UserRepository(dataDir);
        DocumentRepository documentRepository = new DocumentRepository(dataDir);
        CategoryRepository categoryRepository = new CategoryRepository(dataDir);

        Author author = new Author(
                "Alice",
                "Writer",
                "alice",
                "alice123",
                Set.of("Programming")
        );
        author.followDocument("doc-1");
        author.markDocumentVersionSeen("doc-1", 2);
        userRepository.add(author);

        Document document = new Document(
                "doc-1",
                "Java Basics",
                "Alice Writer",
                "Programming",
                "2026-02-23",
                1,
                "v1"
        );
        document.updateContent("v2");
        documentRepository.add(document);

        categoryRepository.add("Programming");
        categoryRepository.add("Multimedia");

        userRepository.save();
        documentRepository.save();
        categoryRepository.save();

        UserRepository loadedUsers = new UserRepository(dataDir);
        DocumentRepository loadedDocuments = new DocumentRepository(dataDir);
        CategoryRepository loadedCategories = new CategoryRepository(dataDir);

        loadedUsers.load();
        loadedDocuments.load();
        loadedCategories.load();

        User loadedAuthor = loadedUsers.findByUsername("alice").orElseThrow();
        assertEquals(UserRole.AUTHOR, loadedAuthor.getRole());
        assertTrue(loadedAuthor.getAllowedCategories().contains("Programming"));
        assertTrue(loadedAuthor.getFollowedDocuments().contains("doc-1"));
        assertEquals(2, loadedAuthor.getLastSeenVersion("doc-1"));

        Document loadedDoc = loadedDocuments.findById("doc-1").orElseThrow();
        assertEquals(2, loadedDoc.getVersion());
        assertEquals("v2", loadedDoc.getContent());
        assertEquals(2, loadedDoc.getVersions().size());

        assertTrue(loadedCategories.findAll().contains("Programming"));
        assertTrue(loadedCategories.findAll().contains("Multimedia"));
        assertFalse(loadedCategories.findAll().contains("Unknown"));
    }
}
