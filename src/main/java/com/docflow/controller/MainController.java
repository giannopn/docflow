/* Ο controller ειναι ενδιαμεσα απο το backend και το frontend.
Βλεπει τι κουμπια πατησε ο χρηστης και καλει το backend */

package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AuthService;
import com.docflow.service.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private ListView<String> documentsList;
    @FXML private Label statusLabel;
    @FXML private Label categoriesCountLabel;
    @FXML private Label documentsCountLabel;
    @FXML private Label watchedCountLabel;

    @FXML
    private void initialize() {
        refreshSummary();
        onLoadDocuments();
    }

    @FXML
    private void onLoadDocuments() {
        documentsList.getItems().clear();

        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        DocumentService documentService = AppState.getInstance().getDocumentService();
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();

        List<Document> documents = currentUser.isPresent()
                ? documentService.listAccessible(currentUser.get())
                : documentRepository.findAll();

        for (Document doc : documents) {
            documentsList.getItems().add(doc.toString());
        }

        statusLabel.setText("Loaded " + documents.size());
    }

    private void refreshSummary() {
        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        int documentsCount = documentRepository.findAll().size();
        int categoriesCount = AppState.getInstance().getCategoryRepository().findAll().size();

        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        int watchedCount = currentUser.map(user -> user.getFollowedDocuments().size()).orElse(0);

        documentsCountLabel.setText(String.valueOf(documentsCount));
        categoriesCountLabel.setText(String.valueOf(categoriesCount));
        watchedCountLabel.setText(String.valueOf(watchedCount));
    }
}
