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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DocumentDialogController {

    private static final String COPY_BUTTON_TEXT = "Copy";
    private static final String COPIED_BUTTON_TEXT = "Copied";

    @FXML private Label titleLabel;
    @FXML private Label authorValueLabel;
    @FXML private Label categoryValueLabel;
    @FXML private Label createdValueLabel;
    @FXML private ComboBox<DocumentVersion> versionSelector;
    @FXML private TextArea contentArea;
    @FXML private Button copyButton;
    @FXML private Label statusLabel;
    @FXML private Label editedLabel;
    @FXML private Button restoreButton;
    @FXML private Button backToLatestButton;
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
            resetCopyButtonLabel();
            updateCopyButtonState();
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
        resetCopyButtonLabel();
        updateCopyButtonState();

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
        selectLatestVersion();
        updateSaveButtonState();
    }

    @FXML
    private void onClose() {
        closeWindow();
    }

    @FXML
    private void onBackToLatest() {
        if (!canEdit) {
            return;
        }
        selectLatestVersion();
    }

    @FXML
    private void onCopyContent() {
        if (contentArea == null) {
            return;
        }
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(contentArea.getText() == null ? "" : contentArea.getText());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
        if (copyButton != null) {
            copyButton.setText(COPIED_BUTTON_TEXT);
        }
    }

    private void updateViewState() {
        DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        if (selectedVersion == null) {
            contentArea.clear();
            contentArea.setEditable(false);
            updateVersionActionButtonsState();
            updateCopyButtonState();
            saveButton.setDisable(true);
            updateEditedLabelState();
            return;
        }

        boolean latestSelected = selectedVersion.getVersionNumber() == latestVersion;
        contentArea.setText(latestSelected ? latestDraftContent : selectedVersion.getContent());
        boolean editable = canEdit && latestSelected;
        contentArea.setEditable(editable);
        updateVersionActionButtonsState();
        updateCopyButtonState();
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

    private void updateVersionActionButtonsState() {
        if (restoreButton == null || backToLatestButton == null) {
            return;
        }
        if (!canEdit) {
            restoreButton.setVisible(false);
            restoreButton.setManaged(false);
            backToLatestButton.setVisible(false);
            backToLatestButton.setManaged(false);
            return;
        }

        DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        boolean canUseVersionActions = selectedVersion != null
                && selectedVersion.getVersionNumber() != latestVersion;
        restoreButton.setManaged(true);
        restoreButton.setVisible(canUseVersionActions);
        backToLatestButton.setManaged(true);
        backToLatestButton.setVisible(canUseVersionActions);
    }

    private boolean hasUnsavedLatestEdits() {
        return !Objects.equals(latestDraftContent, latestContent);
    }

    private void updateCopyButtonState() {
        if (copyButton == null) {
            return;
        }
        String content = contentArea == null ? null : contentArea.getText();
        copyButton.setDisable(content == null || content.isEmpty());
    }

    private void resetCopyButtonLabel() {
        if (copyButton == null) {
            return;
        }
        copyButton.setText(COPY_BUTTON_TEXT);
    }

    private void selectLatestVersion() {
        if (versionSelector == null) {
            return;
        }
        versionSelector.getItems().stream()
                .filter(version -> version.getVersionNumber() == latestVersion)
                .findFirst()
                .ifPresent(version -> versionSelector.getSelectionModel().select(version));
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
