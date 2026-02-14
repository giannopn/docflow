package com.docflow;

import com.docflow.repository.CategoryRepository;
import com.docflow.repository.DocumentRepository;
import com.docflow.repository.UserRepository;

import java.io.IOException;

public final class AppState {

    private static final AppState INSTANCE = new AppState();

    private final UserRepository userRepository = new UserRepository();
    private final DocumentRepository documentRepository = new DocumentRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();

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
