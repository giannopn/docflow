package com.docflow;

import com.docflow.repository.CategoryRepository;
import com.docflow.repository.DocumentRepository;
import com.docflow.repository.UserRepository;
import com.docflow.service.AuthService;
import com.docflow.service.AdminService;
import com.docflow.service.DocumentService;
import com.docflow.service.WatchService;

import java.io.IOException;

public final class AppState {

    private static final AppState INSTANCE = new AppState();

    private final UserRepository userRepository = new UserRepository();
    private final DocumentRepository documentRepository = new DocumentRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final AuthService authService = new AuthService(userRepository);
    private final DocumentService documentService = new DocumentService(documentRepository, userRepository);
    private final WatchService watchService = new WatchService(documentRepository);
    private final AdminService adminService = new AdminService(userRepository, categoryRepository, documentService);

    private AppState() {}

    public static AppState getInstance() {
        return INSTANCE;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public DocumentRepository getDocumentRepository() {
        return documentRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    public WatchService getWatchService() {
        return watchService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public void loadAll() {
        try {
            userRepository.load();
            documentRepository.load();
            categoryRepository.load();
            userRepository.ensureDefaultAdminExists();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application data", e);
        }
    }

    public void saveAll() {
        try {
            userRepository.save();
            documentRepository.save();
            categoryRepository.save();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save application data", e);
        }
    }
}
