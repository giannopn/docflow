package com.docflow;

import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AuthService;

import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("DocFlow backend test");

        AppState appState = AppState.getInstance();
        appState.loadAll();

        AuthService authService = appState.getAuthService();
        Optional<User> admin = authService.login("medialab", "medialab_2025");
        System.out.println("Login admin: " + (admin.isPresent() ? "ok" : "failed"));

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
