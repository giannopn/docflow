package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.User;
import com.docflow.service.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NewDocumentDialogController {

    @FXML private TextField titleField;
    @FXML private ComboBox<String> categorySelector;
    @FXML private TextArea contentArea;
    @FXML private Label wordCountLabel;
    @FXML private Label statusLabel;
    @FXML private Button createButton;

    private User currentUser;
    private DocumentService documentService;
    private boolean created;

    @FXML
    private void initialize() {
        titleField.textProperty().addListener((obs, oldValue, newValue) -> updateCreateButtonState());
        contentArea.textProperty().addListener((obs, oldValue, newValue) -> {
            updateCreateButtonState();
            updateWordCount();
        });
        categorySelector.valueProperty().addListener((obs, oldValue, newValue) -> updateCreateButtonState());
    }

    public void setContext(User currentUser) {
        this.currentUser = currentUser;
        this.documentService = AppState.getInstance().getDocumentService();
        this.created = false;
        statusLabel.setText("");

        List<String> categories = new ArrayList<>();
        for (String category : AppState.getInstance().getCategoryRepository().findAll()) {
            if (currentUser.hasAccessToCategory(category)) {
                categories.add(category);
            }
        }
        categories.sort(Comparator.naturalOrder());
        categorySelector.getItems().setAll(categories);
        if (!categories.isEmpty()) {
            categorySelector.getSelectionModel().selectFirst();
        }

        updateCreateButtonState();
        updateWordCount();
    }

    public boolean isCreated() {
        return created;
    }

    @FXML
    private void onCreate() {
        if (currentUser == null) {
            return;
        }

        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String category = categorySelector.getValue();
        String content = contentArea.getText() == null ? "" : contentArea.getText().trim();

        try {
            documentService.create(currentUser, title, category, content);
            created = true;
            closeWindow();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    @FXML
    private void onPasteClipboard() {
        if (contentArea == null) {
            return;
        }
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            return;
        }
        contentArea.paste();
    }

    private void updateCreateButtonState() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String content = contentArea.getText() == null ? "" : contentArea.getText().trim();
        boolean valid = !title.isBlank()
                && categorySelector.getValue() != null
                && !content.isBlank();
        createButton.setDisable(!valid);
    }

    private void updateWordCount() {
        if (wordCountLabel == null) {
            return;
        }
        String content = contentArea == null || contentArea.getText() == null
                ? ""
                : contentArea.getText().trim();
        int count = content.isEmpty() ? 0 : content.split("\\s+").length;
        wordCountLabel.setText("Words: " + count);
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
