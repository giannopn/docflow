package com.docflow.service;

import com.docflow.model.Admin;
import com.docflow.model.Author;
import com.docflow.model.Document;
import com.docflow.model.DocumentVersion;
import com.docflow.model.SimpleUser;
import com.docflow.model.User;
import com.docflow.repository.CategoryRepository;
import com.docflow.repository.DocumentRepository;
import com.docflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBehaviorTest {

    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private DocumentRepository documentRepository;
    private CategoryRepository categoryRepository;

    private DocumentService documentService;
    private WatchService watchService;
    private AdminService adminService;

    private Admin admin;
    private Author author;
    private SimpleUser simpleUser;

    @BeforeEach
    void setUp() {
        Path dataDir = tempDir.resolve("medialab");
        userRepository = new UserRepository(dataDir);
        documentRepository = new DocumentRepository(dataDir);
        categoryRepository = new CategoryRepository(dataDir);

        documentService = new DocumentService(documentRepository, userRepository);
        watchService = new WatchService(documentRepository);
        adminService = new AdminService(userRepository, categoryRepository, documentService);

        admin = new Admin("System", "Admin", "admin", "admin123", Set.of());
        author = new Author("Alice", "Writer", "alice", "alice123", Set.of("Programming"));
        simpleUser = new SimpleUser("Bob", "Reader", "bob", "bob123", Set.of("Programming"));

        userRepository.add(admin);
        userRepository.add(author);
        userRepository.add(simpleUser);

        categoryRepository.add("Programming");
        categoryRepository.add("Multimedia");
    }

    @Test
    void shouldEnforceDocumentCreationPermissionsByRole() {
        assertThrows(
                IllegalStateException.class,
                () -> documentService.create(simpleUser, "Forbidden", "Programming", "content")
        );

        Document created = documentService.create(author, "Java Basics", "Programming", "v1");
        assertEquals("Java Basics", created.getTitle());

        assertThrows(
                IllegalStateException.class,
                () -> documentService.create(author, "Out of scope", "Multimedia", "content")
        );
    }

    @Test
    void shouldCreateNewVersionOnDocumentUpdate() {
        Document created = documentService.create(author, "Versioned Doc", "Programming", "v1");
        documentService.updateContent(author, created.getId(), "v2");
        documentService.updateContent(author, created.getId(), "v3");

        Document reloaded = documentRepository.findById(created.getId()).orElseThrow();
        assertEquals(3, reloaded.getVersion());
        assertEquals("v3", reloaded.getContent());
        assertEquals(3, reloaded.getVersions().size());
        assertEquals("v1", reloaded.getVersions().get(0).getContent());

        List<DocumentVersion> visibleToSimple = documentService.getVisibleVersions(simpleUser, reloaded);
        List<DocumentVersion> visibleToAuthor = documentService.getVisibleVersions(author, reloaded);
        assertEquals(1, visibleToSimple.size());
        assertEquals(3, visibleToAuthor.size());
        assertEquals(3, visibleToSimple.get(0).getVersionNumber());
    }

    @Test
    void shouldSearchByTitleAuthorAndCategoryWithinAllowedAccess() {
        documentService.create(author, "Java Basics", "Programming", "A");
        documentService.create(author, "JavaFX Guide", "Programming", "B");

        Document multimediaDoc = new Document(
                "doc-mm",
                "Multimedia Intro",
                "Other Author",
                "Multimedia",
                "2026-02-23",
                1,
                "C"
        );
        documentRepository.add(multimediaDoc);

        assertEquals(2, documentService.search(simpleUser, "java", "", "").size());
        assertEquals(2, documentService.search(simpleUser, "", "alice", "").size());
        assertEquals(0, documentService.search(simpleUser, "", "", "multimedia").size());
        assertEquals(1, documentService.search(admin, "", "", "multimedia").size());
    }

    @Test
    void shouldHandleFollowUnfollowAndUpdateNotifications() {
        Document document = documentService.create(author, "Watch Me", "Programming", "v1");

        watchService.follow(simpleUser, document.getId());
        assertTrue(simpleUser.isFollowing(document.getId()));
        assertEquals(0, watchService.listUpdatedSinceLastSeen(simpleUser).size());

        documentService.updateContent(author, document.getId(), "v2");
        assertEquals(1, watchService.listUpdatedSinceLastSeen(simpleUser).size());

        watchService.markAllSeen(simpleUser);
        assertEquals(0, watchService.listUpdatedSinceLastSeen(simpleUser).size());

        watchService.unfollow(simpleUser, document.getId());
        assertFalse(simpleUser.isFollowing(document.getId()));
    }

    @Test
    void shouldDeleteCategoryWithDocumentAndWatchCleanup() {
        Document document = documentService.create(author, "Cascade Doc", "Programming", "v1");
        watchService.follow(simpleUser, document.getId());

        boolean removed = adminService.deleteCategory(admin, "Programming");
        assertTrue(removed);

        assertFalse(categoryRepository.findAll().contains("Programming"));
        assertTrue(documentRepository.findById(document.getId()).isEmpty());
        assertFalse(simpleUser.getAllowedCategories().contains("Programming"));
        assertFalse(simpleUser.isFollowing(document.getId()));
    }
}
