/* Ο controller ειναι ενδιαμεσα απο το backend και το frontend.
Βλεπει τι κουμπια πατησε ο χρηστης και καλει το backend */

package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.repository.DocumentRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class MainController {

    @FXML private ListView<String> documentsList;
    @FXML private Label statusLabel;

    @FXML
    private void onLoadDocuments() {
        documentsList.getItems().clear();

        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        List<Document> documents = documentRepository.findAll();
        for (Document doc : documents) {
            documentsList.getItems().add(doc.toString());
        }

        statusLabel.setText("Loaded " + documents.size());
    }
}
