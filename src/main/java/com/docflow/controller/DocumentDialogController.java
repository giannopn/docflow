package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.DocumentVersion;
import com.docflow.model.User;
import com.docflow.service.DocumentService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import java.util.Optional;

public class DocumentDialogController {

    @FXML private Label titleLabel;
    @FXML private Label authorValueLabel;
    @FXML private Label categoryValueLabel;
    @FXML private Label createdValueLabel;
    @FXML private ComboBox<DocumentVersion> versionSelector;
    @FXML private TextArea contentArea;
    @FXML private Label statusLabel;
    @FXML private Label editedLabel;
    @FXML private Button restoreButton;
    @FXML private Button saveButton;

    private Document document;
    private User currentUser;
    private DocumentService documentService;
    private int latestVersion;
    private String latestContent;
    private String latestDraftContent;
    private boolean canEdit;
    private boolean saved;

    @FXML
    private void initialize() {
        versionSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> updateViewState());
        contentArea.textProperty().addListener((obs, oldValue, newValue) -> {
            DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
            if (canEdit
                    && selectedVersion != null
                    && selectedVersion.getVersionNumber() == latestVersion) {
                latestDraftContent = newValue;
            }
            updateSaveButtonState();
            updateEditedLabelState();
        });
    }

    public void setContext(Document document, User currentUser) {
        this.document = document;
        this.currentUser = currentUser;
        this.documentService = AppState.getInstance().getDocumentService();
        this.canEdit = currentUser.canManageDocuments();
        this.latestVersion = document.getVersion();
        this.latestContent = document.getContent();
        this.latestDraftContent = latestContent;
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
        editedLabel.setVisible(false);
        editedLabel.setManaged(canEdit);
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
    private void onRestore() {
        if (document == null || currentUser == null || !canEdit) {
            return;
        }

        DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        if (selectedVersion == null || selectedVersion.getVersionNumber() == latestVersion) {
            return;
        }

        if (hasUnsavedLatestEdits() && !confirmRestoreReplacement()) {
            return;
        }

        latestDraftContent = selectedVersion.getContent();
        versionSelector.getItems().stream()
                .filter(version -> version.getVersionNumber() == latestVersion)
                .findFirst()
                .ifPresent(version -> versionSelector.getSelectionModel().select(version));
        updateSaveButtonState();
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
            updateRestoreButtonState();
            saveButton.setDisable(true);
            updateEditedLabelState();
            return;
        }

        boolean latestSelected = selectedVersion.getVersionNumber() == latestVersion;
        contentArea.setText(latestSelected ? latestDraftContent : selectedVersion.getContent());
        boolean editable = canEdit && latestSelected;
        contentArea.setEditable(editable);
        updateRestoreButtonState();
        updateSaveButtonState();
        updateEditedLabelState();
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

    private void updateRestoreButtonState() {
        if (restoreButton == null) {
            return;
        }
        if (!canEdit) {
            restoreButton.setVisible(false);
            restoreButton.setManaged(false);
            return;
        }

        DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        boolean canRestore = selectedVersion != null
                && selectedVersion.getVersionNumber() != latestVersion;
        restoreButton.setManaged(true);
        restoreButton.setVisible(canRestore);
    }

    private boolean hasUnsavedLatestEdits() {
        return !Objects.equals(latestDraftContent, latestContent);
    }

    private void updateEditedLabelState() {
        if (editedLabel == null || !canEdit) {
            return;
        }
        editedLabel.setVisible(hasUnsavedLatestEdits());
    }

    private boolean confirmRestoreReplacement() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved changes");
        alert.setHeaderText("Replace unsaved changes?");
        alert.setContentText("Your unsaved edits will be replaced.");
        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    private void closeWindow() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
