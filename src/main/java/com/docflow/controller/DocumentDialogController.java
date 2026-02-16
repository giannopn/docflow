package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.DocumentVersion;
import com.docflow.model.User;
import com.docflow.service.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DocumentDialogController {

    @FXML private Label titleLabel;
    @FXML private Label authorValueLabel;
    @FXML private Label categoryValueLabel;
    @FXML private Label createdValueLabel;
    @FXML private ComboBox<DocumentVersion> versionSelector;
    @FXML private TextArea contentArea;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    private Document document;
    private User currentUser;
    private DocumentService documentService;
    private int latestVersion;
    private String latestContent;
    private boolean canEdit;
    private boolean saved;

    @FXML
    private void initialize() {
        versionSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> updateViewState());
        contentArea.textProperty().addListener((obs, oldValue, newValue) -> updateSaveButtonState());
    }

    public void setContext(Document document, User currentUser) {
        this.document = document;
        this.currentUser = currentUser;
        this.documentService = AppState.getInstance().getDocumentService();
        this.canEdit = currentUser.canManageDocuments();
        this.latestVersion = document.getVersion();
        this.latestContent = document.getContent();
        this.saved = false;

        titleLabel.setText(document.getTitle());
        authorValueLabel.setText(document.getAuthor());
        categoryValueLabel.setText(document.getCategory());
        createdValueLabel.setText(document.getCreatedAt());
        statusLabel.setText("");

        List<DocumentVersion> visibleVersions = new ArrayList<>(documentService.getVisibleVersions(currentUser, document));
        visibleVersions.sort(Comparator.comparingInt(DocumentVersion::getVersionNumber).reversed());
        versionSelector.getItems().setAll(visibleVersions);
        versionSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(DocumentVersion version) {
                if (version == null) {
                    return "";
                }
                return version.getVersionNumber() == latestVersion
                        ? "v" + version.getVersionNumber() + " (latest)"
                        : "v" + version.getVersionNumber() + " (read-only)";
            }

            @Override
            public DocumentVersion fromString(String string) {
                return null;
            }
        });
        versionSelector.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(DocumentVersion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : versionSelector.getConverter().toString(item));
            }
        });
        if (!visibleVersions.isEmpty()) {
            versionSelector.getSelectionModel().selectFirst();
        }

        versionSelector.setDisable(!canEdit);
        saveButton.setVisible(canEdit);
        saveButton.setManaged(canEdit);

        updateViewState();
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        if (document == null || currentUser == null) {
            return;
        }
        try {
            documentService.updateContent(currentUser, document.getId(), contentArea.getText());
            saved = true;
            closeWindow();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onClose() {
        closeWindow();
    }

    private void updateViewState() {
        DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        if (selectedVersion == null) {
            contentArea.clear();
            contentArea.setEditable(false);
            saveButton.setDisable(true);
            return;
        }

        boolean latestSelected = selectedVersion.getVersionNumber() == latestVersion;
        contentArea.setText(selectedVersion.getContent());
        boolean editable = canEdit && latestSelected;
        contentArea.setEditable(editable);
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        if (selectedVersion == null || !canEdit) {
            saveButton.setDisable(true);
            return;
        }

        boolean latestSelected = selectedVersion.getVersionNumber() == latestVersion;
        boolean unchanged = Objects.equals(contentArea.getText(), latestContent);
        saveButton.setDisable(!latestSelected || unchanged);
    }

    private void closeWindow() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
