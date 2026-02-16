/* Ο controller ειναι ενδιαμεσα απο το backend και το frontend.
Βλεπει τι κουμπια πατησε ο χρηστης και καλει το backend */

package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.DocumentVersion;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AdminService;
import com.docflow.service.AuthService;
import com.docflow.service.DocumentService;
import com.docflow.service.WatchService;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

public class MainController {

    @FXML private TableView<Document> documentsTable;
    @FXML private TableColumn<Document, String> titleColumn;
    @FXML private TableColumn<Document, String> authorColumn;
    @FXML private TableColumn<Document, String> categoryColumn;
    @FXML private TableColumn<Document, String> createdAtColumn;
    @FXML private TableColumn<Document, Number> versionColumn;
    @FXML private TableColumn<Document, Boolean> followingColumn;
    @FXML private Label statusLabel;
    @FXML private Label categoriesCountLabel;
    @FXML private Label documentsCountLabel;
    @FXML private Label watchedCountLabel;
    @FXML private TextField searchTitleField;
    @FXML private TextField searchAuthorField;
    @FXML private TextField searchCategoryField;
    @FXML private TextField createTitleField;
    @FXML private TextField createCategoryField;
    @FXML private TextField createContentField;
    @FXML private ListView<String> watchList;
    @FXML private ListView<String> usersList;
    @FXML private ListView<String> categoriesList;
    @FXML private Label watchStatusLabel;
    @FXML private Label updatedWatchLabel;
    @FXML private Label usersStatusLabel;
    @FXML private Label categoriesStatusLabel;
    @FXML private TextField categoryNameField;
    @FXML private TextField categoryRenameField;
    @FXML private TextField userFirstNameField;
    @FXML private TextField userLastNameField;
    @FXML private TextField userUsernameField;
    @FXML private TextField userPasswordField;
    @FXML private TextField userRoleField;
    @FXML private TextField userCategoriesField;
    @FXML private Tab usersTab;
    @FXML private Tab categoriesTab;

    @FXML
    private void initialize() {
        configureDocumentTable();
        refreshSummary();
        applyRoleVisibility();
        onLoadDocuments();
    }

    private void configureDocumentTable() {
        titleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getTitle()));
        authorColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAuthor()));
        categoryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCategory()));
        createdAtColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCreatedAt()));
        versionColumn.setCellValueFactory(cellData -> new ReadOnlyIntegerWrapper(cellData.getValue().getVersion()));
        followingColumn.setCellValueFactory(cellData ->
                new ReadOnlyBooleanWrapper(isFollowedByCurrentUser(cellData.getValue())));
        followingColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean followed, boolean empty) {
                super.updateItem(followed, empty);
                if (empty || followed == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label badge = new Label("\uD83D\uDC41");
                badge.setStyle(
                        followed
                                ? "-fx-font-size: 14px; -fx-text-fill: #2563eb;"
                                : "-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-opacity: 0.45;"
                );
                setText(null);
                setGraphic(badge);
            }
        });

        documentsTable.setRowFactory(table -> {
            TableRow<Document> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    documentsTable.getSelectionModel().select(row.getItem());
                    onOpenDocument();
                }
            });
            MenuItem openItem = new MenuItem("Open");
            openItem.setOnAction(event -> {
                documentsTable.getSelectionModel().select(row.getItem());
                onOpenDocument();
            });

            MenuItem followItem = new MenuItem("Follow");
            followItem.setOnAction(event -> {
                documentsTable.getSelectionModel().select(row.getItem());
                onFollowSelectedDocument();
            });

            MenuItem unfollowItem = new MenuItem("Unfollow");
            unfollowItem.setOnAction(event -> {
                Document doc = row.getItem();
                if (doc == null) {
                    return;
                }
                AuthService authService = AppState.getInstance().getAuthService();
                Optional<User> currentUser = authService.getCurrentUser();
                if (currentUser.isEmpty()) {
                    return;
                }
                WatchService watchService = AppState.getInstance().getWatchService();
                watchService.unfollow(currentUser.get(), doc.getId());
                documentsTable.refresh();
                onLoadWatchlist();
                refreshSummary();
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                documentsTable.getSelectionModel().select(row.getItem());
                onDeleteDocument();
            });

            ContextMenu contextMenu = new ContextMenu(openItem, followItem, unfollowItem, deleteItem);
            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu));

            return row;
        });
    }

    private boolean isFollowedByCurrentUser(Document document) {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        return currentUser.isPresent() && currentUser.get().getFollowedDocuments().contains(document.getId());
    }

    @FXML
    private void onLoadDocuments() {
        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        DocumentService documentService = AppState.getInstance().getDocumentService();
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();

        List<Document> documents = currentUser.isPresent()
                ? documentService.listAccessible(currentUser.get())
                : documentRepository.findAll();

        documentsTable.getItems().setAll(documents);

        statusLabel.setText("Loaded " + documents.size());
    }

    @FXML
    private void onFollowSelectedDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        WatchService watchService = AppState.getInstance().getWatchService();
        watchService.follow(currentUser.get(), selected.getId());
        documentsTable.refresh();

        refreshSummary();
    }

    @FXML
    private void onSearch() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        List<Document> results = documentService.search(
                currentUser.get(),
                searchTitleField.getText(),
                searchAuthorField.getText(),
                searchCategoryField.getText()
        );
        documentsTable.getItems().setAll(results);
        statusLabel.setText("Found " + results.size());
    }

    @FXML
    private void onCreateDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageDocuments()) {
            statusLabel.setText("Author/Admin only");
            return;
        }

        String title = createTitleField.getText();
        String category = createCategoryField.getText();
        String content = createContentField.getText();

        DocumentService documentService = AppState.getInstance().getDocumentService();
        try {
            documentService.create(currentUser.get(), title, category, content);
            statusLabel.setText("Created");
            onLoadDocuments();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onOpenDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a document");
            return;
        }

        showDocumentWindow(selected, currentUser.get());
    }

    @FXML
    private void onEditDocument() {
        onOpenDocument();
    }

    @FXML
    private void onDeleteDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageDocuments()) {
            statusLabel.setText("Author/Admin only");
            return;
        }

        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a document");
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        try {
            boolean removed = documentService.delete(currentUser.get(), selected.getId());
            statusLabel.setText(removed ? "Deleted" : "Delete failed");
            onLoadDocuments();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onLoadWatchlist() {
        watchList.getItems().clear();
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        WatchService watchService = AppState.getInstance().getWatchService();
        List<Document> docs = watchService.listFollowed(currentUser.get());
        for (Document doc : docs) {
            watchList.getItems().add(doc.toString());
        }
        watchStatusLabel.setText("Loaded " + docs.size());

        int updatedCount = watchService.listUpdatedSinceLastSeen(currentUser.get()).size();
        updatedWatchLabel.setText("Updated: " + updatedCount);
    }

    @FXML
    private void onFollowSelected() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        WatchService watchService = AppState.getInstance().getWatchService();
        watchService.follow(currentUser.get(), selected.getId());
        documentsTable.refresh();

        onLoadWatchlist();
        refreshSummary();
    }

    @FXML
    private void onUnfollowSelected() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        String selected = watchList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            return;
        }

        WatchService watchService = AppState.getInstance().getWatchService();
        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        for (Document doc : documentRepository.findAll()) {
            if (selected.equals(doc.toString())) {
                watchService.unfollow(currentUser.get(), doc.getId());
                break;
            }
        }

        documentsTable.refresh();
        onLoadWatchlist();
        refreshSummary();
    }

    @FXML
    private void onLoadUsers() {
        usersList.getItems().clear();
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<User> users = adminService.listUsers(currentUser.get());
        for (User user : users) {
            usersList.getItems().add(user.getUsername() + " (" + user.getRole() + ")");
        }
        usersStatusLabel.setText("Loaded " + users.size());
    }

    @FXML
    private void onAddUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        try {
            adminService.addUser(
                    currentUser.get(),
                    userFirstNameField.getText(),
                    userLastNameField.getText(),
                    userUsernameField.getText(),
                    userPasswordField.getText(),
                    parseRole(userRoleField.getText()),
                    parseCategories(userCategoriesField.getText())
            );
            usersStatusLabel.setText("Added");
            onLoadUsers();
        } catch (RuntimeException ex) {
            usersStatusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onEditUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        String username = userUsernameField.getText();
        if (username == null || username.isBlank()) {
            usersStatusLabel.setText("Username required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        try {
            adminService.updateUser(
                    currentUser.get(),
                    username,
                    userPasswordField.getText(),
                    parseRoleNullable(userRoleField.getText()),
                    parseCategories(userCategoriesField.getText())
            );
            usersStatusLabel.setText("Updated");
            onLoadUsers();
        } catch (RuntimeException ex) {
            usersStatusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onDeleteUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        String username = userUsernameField.getText();
        if (username == null || username.isBlank()) {
            usersStatusLabel.setText("Username required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        boolean removed = adminService.deleteUser(currentUser.get(), username);
        usersStatusLabel.setText(removed ? "Deleted" : "Not found");
        onLoadUsers();
    }

    @FXML
    private void onLoadCategories() {
        categoriesList.getItems().clear();
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            categoriesStatusLabel.setText("Admin only");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<String> categories = adminService.listCategories(currentUser.get());
        categoriesList.getItems().addAll(categories);
        categoriesStatusLabel.setText("Loaded " + categories.size());
    }

    @FXML
    private void onAddCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            categoriesStatusLabel.setText("Admin only");
            return;
        }

        String name = categoryNameField.getText();
        if (name == null || name.isBlank()) {
            categoriesStatusLabel.setText("Category required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        try {
            adminService.addCategory(currentUser.get(), name);
            categoriesStatusLabel.setText("Added");
            onLoadCategories();
        } catch (RuntimeException ex) {
            categoriesStatusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onRenameCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            categoriesStatusLabel.setText("Admin only");
            return;
        }

        String oldName = categoryNameField.getText();
        String newName = categoryRenameField.getText();
        if (oldName == null || oldName.isBlank() || newName == null || newName.isBlank()) {
            categoriesStatusLabel.setText("Old and new names required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        boolean renamed = adminService.renameCategory(currentUser.get(), oldName, newName);
        categoriesStatusLabel.setText(renamed ? "Renamed" : "Not found");
        onLoadCategories();
    }

    @FXML
    private void onDeleteCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            categoriesStatusLabel.setText("Admin only");
            return;
        }

        String name = categoryNameField.getText();
        if (name == null || name.isBlank()) {
            categoriesStatusLabel.setText("Category required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        boolean removed = adminService.deleteCategory(currentUser.get(), name);
        categoriesStatusLabel.setText(removed ? "Deleted" : "Not found");
        onLoadCategories();
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

    private void applyRoleVisibility() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        boolean isAdmin = currentUser.isPresent() && currentUser.get().canManageUsers();

        if (usersTab != null) {
            usersTab.setDisable(!isAdmin);
        }
        if (categoriesTab != null) {
            categoriesTab.setDisable(!isAdmin);
        }
    }

    private UserRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }

    private UserRole parseRoleNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return UserRole.valueOf(value.trim().toUpperCase());
    }

    private Set<String> parseCategories(String value) {
        Set<String> result = new LinkedHashSet<>();
        if (value == null || value.isBlank()) {
            return result;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private void showDocumentWindow(Document document, User currentUser) {
        boolean canEdit = currentUser.canManageDocuments();
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        DocumentService documentService = AppState.getInstance().getDocumentService();
        List<DocumentVersion> visibleVersions = new ArrayList<>(documentService.getVisibleVersions(currentUser, document));
        visibleVersions.sort(Comparator.comparingInt(DocumentVersion::getVersionNumber).reversed());
        int latestVersion = document.getVersion();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(document.getTitle());
        dialog.setHeaderText(null);
        DialogPane pane = dialog.getDialogPane();
        pane.setPrefWidth(760);
        pane.setPrefHeight(560);

        Label titleLabel = new Label(document.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700;");

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(10);
        metaGrid.setVgap(8);
        metaGrid.add(new Label("Author:"), 0, 0);
        metaGrid.add(new Label(document.getAuthor()), 1, 0);
        metaGrid.add(new Label("Category:"), 0, 1);
        metaGrid.add(new Label(document.getCategory()), 1, 1);
        metaGrid.add(new Label("Created:"), 0, 2);
        metaGrid.add(new Label(document.getCreatedAt()), 1, 2);

        ComboBox<DocumentVersion> versionSelector = new ComboBox<>();
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

        Label viewVersionLabel = new Label("View version:");
        VBox versionBox = new VBox(4, viewVersionLabel, versionSelector);
        versionBox.setAlignment(Pos.TOP_RIGHT);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox detailsRow = new HBox(12, metaGrid, headerSpacer, versionBox);
        detailsRow.setAlignment(Pos.TOP_LEFT);

        TextArea contentArea = new TextArea(document.getContent());
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(18);

        VBox root = new VBox(10, titleLabel, detailsRow, contentArea);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(10));
        pane.setContent(root);

        Node saveButton = null;
        if (canEdit) {
            pane.getButtonTypes().addAll(ButtonType.CANCEL, saveButtonType);
            saveButton = pane.lookupButton(saveButtonType);
        } else {
            pane.getButtonTypes().add(ButtonType.CLOSE);
        }

        Node finalSaveButton = saveButton;
        Runnable updateViewState = () -> {
            DocumentVersion selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) {
                return;
            }

            boolean latestSelected = selectedVersion.getVersionNumber() == latestVersion;
            contentArea.setText(selectedVersion.getContent());
            boolean editable = canEdit && latestSelected;
            contentArea.setEditable(editable);
            if (finalSaveButton != null) {
                finalSaveButton.setDisable(!editable);
            }
        };
        updateViewState.run();
        versionSelector.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> updateViewState.run());

        Optional<ButtonType> result = dialog.showAndWait();
        if (!canEdit || result.isEmpty() || result.get() != saveButtonType) {
            return;
        }

        try {
            documentService.updateContent(currentUser, document.getId(), contentArea.getText());
            statusLabel.setText("Updated");
            onLoadDocuments();
            onLoadWatchlist();
            refreshSummary();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }
}
