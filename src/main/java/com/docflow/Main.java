package com.docflow;

import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AdminService;
import com.docflow.service.AuthService;
import com.docflow.service.DocumentService;
import com.docflow.service.WatchService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("DocFlow backend test");

        AppState appState = AppState.getInstance();
        appState.loadAll();

        AuthService authService = appState.getAuthService();
        Optional<User> admin = authService.login("medialab", "medialab_2025");
        System.out.println("Login admin: " + (admin.isPresent() ? "ok" : "failed"));

        if (admin.isPresent()) {
            DocumentService documentService = appState.getDocumentService();
            WatchService watchService = appState.getWatchService();
            AdminService adminService = appState.getAdminService();

            List<Document> accessible = documentService.listAccessible(admin.get());
            System.out.println("Accessible documents: " + accessible.size());

            List<Document> updated = watchService.listUpdatedSinceLastSeen(admin.get());
            System.out.println("Updated watched documents: " + updated.size());

            List<String> categories = adminService.listCategories(admin.get());
            System.out.println("Categories: " + categories);

            if (!categories.contains("General")) {
                adminService.addCategory(admin.get(), "General");
                System.out.println("Added category: General");
            }

            if (!categories.contains("Computer Science")) {
                adminService.addCategory(admin.get(), "Computer Science");
                System.out.println("Added category: Computer Science");
            }

            Set<String> userCategories = new HashSet<>(categories);
            userCategories.add("General");
            adminService.addUser(
                    admin.get(),
                    "Test",
                    "User",
                    "testuser",
                    "test123",
                    UserRole.SIMPLE_USER,
                    userCategories
            );
            System.out.println("Added user: testuser");

            List<Document> searchResults = documentService.search(admin.get(), "Java", "", "");
            System.out.println("Search results (title contains 'Java'): " + searchResults.size());

            if (!searchResults.isEmpty()) {
                Document doc = searchResults.get(0);
                watchService.follow(admin.get(), doc.getId());
                System.out.println("Followed document: " + doc.getTitle());
                watchService.markAllSeen(admin.get());
            }
        }

        DocumentRepository documentRepository = appState.getDocumentRepository();
        List<Document> docs = documentRepository.findAll();

        System.out.println("Loaded: " + docs.size());
        for (Document d : docs) {
            System.out.println("- " + d);
        }

        authService.logout();
        appState.saveAll();
    }
}
