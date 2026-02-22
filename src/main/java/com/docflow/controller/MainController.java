/* Ο controller ειναι ενδιαμεσα απο το backend και το frontend.
Βλεπει τι κουμπια πατησε ο χρηστης και καλει το backend */

package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AdminService;
import com.docflow.service.AuthService;
import com.docflow.service.DocumentService;
import com.docflow.service.WatchService;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainController {
    private static final String ALL_CATEGORIES_OPTION = "All categories";

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
    @FXML private ComboBox<String> searchCategoryCombo;
    @FXML private CheckBox followingOnlyCheck;
    @FXML private Button newDocumentButton;
    @FXML private ListView<Document> watchList;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userFirstNameColumn;
    @FXML private TableColumn<User, String> userLastNameColumn;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userCategoriesColumn;
    @FXML private TableView<CategoryRow> categoriesTable;
    @FXML private TableColumn<CategoryRow, String> categoryNameColumn;
    @FXML private TableColumn<CategoryRow, Number> categoryDocumentsColumn;
    @FXML private Label watchStatusLabel;
    @FXML private Label updatedWatchLabel;
    @FXML private Label usersStatusLabel;
    @FXML private Label categoriesStatusLabel;
    @FXML private Button adminSettingsButton;
    @FXML private TabPane workspaceTabPane;
    @FXML private TabPane adminTabPane;
    @FXML private Tab documentsTab;
    @FXML private Tab watchlistTab;
    @FXML private Tab usersTab;
    @FXML private Tab categoriesTab;

    private boolean adminSettingsVisible;

    @FXML
    private void initialize() {
        configureDocumentTable();
        configureUsersTable();
        configureCategoriesTable();
        if (followingOnlyCheck != null) {
            followingOnlyCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> onSearch());
        }
        adminSettingsVisible = false;
        refreshSummary();
        applyRoleVisibility();
        configureTabAutoRefresh();
        populateSearchCategories();
        onLoadDocuments();
    }

    private void configureTabAutoRefresh() {
        if (documentsTab != null) {
            documentsTab.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (Boolean.TRUE.equals(isSelected)) {
                    onLoadDocuments();
                }
            });
        }
        if (watchlistTab != null) {
            watchlistTab.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (Boolean.TRUE.equals(isSelected)) {
                    onLoadWatchlist();
                }
            });
        }
        if (usersTab != null) {
            usersTab.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (Boolean.TRUE.equals(isSelected)) {
                    onLoadUsers();
                }
            });
        }
        if (categoriesTab != null) {
            categoriesTab.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (Boolean.TRUE.equals(isSelected)) {
                    onLoadCategories();
                }
            });
        }
    }

    private void configureDocumentTable() {
        documentsTable.setPlaceholder(new Label(""));
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
                if (isFollowedByCurrentUser(doc)) {
                    watchService.unfollow(currentUser.get(), doc.getId());
                } else {
                    watchService.follow(currentUser.get(), doc.getId());
                }
                documentsTable.refresh();
                onLoadWatchlist();
                refreshSummary();
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                documentsTable.getSelectionModel().select(row.getItem());
                onDeleteDocument();
            });

            ContextMenu contextMenu = new ContextMenu(openItem, followItem, deleteItem);
            contextMenu.setOnShowing(event -> {
                Document doc = row.getItem();
                if (doc == null) {
                    followItem.setText("Follow");
                    return;
                }
                followItem.setText(isFollowedByCurrentUser(doc) ? "Unfollow" : "Follow");
            });
            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu));

            return row;
        });
    }

    private void configureCategoriesTable() {
        if (categoriesTable == null) {
            return;
        }

        categoriesTable.setPlaceholder(new Label(""));
        categoryNameColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getName()));
        categoryDocumentsColumn.setCellValueFactory(cellData ->
                new ReadOnlyIntegerWrapper(cellData.getValue().getDocumentCount()));

        categoriesTable.setRowFactory(table -> {
            TableRow<CategoryRow> row = new TableRow<>();

            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(event -> {
                categoriesTable.getSelectionModel().select(row.getItem());
                onRenameCategory();
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                categoriesTable.getSelectionModel().select(row.getItem());
                onDeleteCategory();
            });

            ContextMenu contextMenu = new ContextMenu(renameItem, deleteItem);
            contextMenu.setOnShowing(event -> {
                AuthService authService = AppState.getInstance().getAuthService();
                Optional<User> currentUser = authService.getCurrentUser();
                boolean isAdmin = currentUser.isPresent() && currentUser.get().canManageCategories();
                renameItem.setDisable(!isAdmin);
                deleteItem.setDisable(!isAdmin);
            });

            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu));
            return row;
        });
    }

    private void configureUsersTable() {
        if (usersTable == null) {
            return;
        }

        usersTable.setPlaceholder(new Label(""));
        userFirstNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getFirstName()));
        userLastNameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getLastName()));
        userUsernameColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUsername()));
        userRoleColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRole().name()));
        userCategoriesColumn.setCellValueFactory(cellData -> {
            List<String> categories = new ArrayList<>(cellData.getValue().getAllowedCategories());
            categories.sort(Comparator.naturalOrder());
            return new ReadOnlyStringWrapper(String.join(", ", categories));
        });

        usersTable.setRowFactory(table -> {
            TableRow<User> row = new TableRow<>();

            MenuItem editItem = new MenuItem("Edit");
            editItem.setOnAction(event -> {
                usersTable.getSelectionModel().select(row.getItem());
                onEditUser();
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                usersTable.getSelectionModel().select(row.getItem());
                onDeleteUser();
            });

            ContextMenu contextMenu = new ContextMenu(editItem, deleteItem);
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
        populateSearchCategories();

        List<Document> documents = currentUser.isPresent()
                ? documentService.listAccessible(currentUser.get())
                : documentRepository.findAll();
        documents = applyFollowingFilter(currentUser, documents);

        documentsTable.getItems().setAll(documents);
        updateResultsStatus(documents.size());
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

        String selectedCategory = searchCategoryCombo.getValue();
        String categoryFilter = (selectedCategory == null || ALL_CATEGORIES_OPTION.equals(selectedCategory))
                ? ""
                : selectedCategory;

        DocumentService documentService = AppState.getInstance().getDocumentService();
        List<Document> results = documentService.search(
                currentUser.get(),
                searchTitleField.getText(),
                searchAuthorField.getText(),
                categoryFilter
        );
        results = applyFollowingFilter(currentUser, results);
        documentsTable.getItems().setAll(results);
        updateResultsStatus(results.size());
    }

    @FXML
    private void onClearSearch() {
        searchTitleField.clear();
        searchAuthorField.clear();
        searchCategoryCombo.setValue(ALL_CATEGORIES_OPTION);
        if (followingOnlyCheck != null) {
            followingOnlyCheck.setSelected(false);
        }
        onLoadDocuments();
    }

    @FXML
    private void onCreateDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageDocuments()) {
            return;
        }

        showNewDocumentWindow(currentUser.get());
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
            return;
        }

        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        try {
            documentService.delete(currentUser.get(), selected.getId());
            onLoadDocuments();
            onLoadWatchlist();
            refreshSummary();
        } catch (RuntimeException ex) {
            updateResultsStatus(documentsTable.getItems().size());
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
        watchList.getItems().setAll(docs);
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

        Document selected = watchList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        WatchService watchService = AppState.getInstance().getWatchService();
        watchService.unfollow(currentUser.get(), selected.getId());

        documentsTable.refresh();
        onLoadWatchlist();
        refreshSummary();
    }

    @FXML
    private void onLoadUsers() {
        if (usersTable != null) {
            usersTable.getItems().clear();
        }
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<User> users = adminService.listUsers(currentUser.get());
        if (usersTable != null) {
            usersTable.getItems().setAll(users);
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

        showAddUserWindow(currentUser.get());
    }

    @FXML
    private void onEditUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            usersStatusLabel.setText("Select a user first");
            return;
        }

        showEditUserWindow(currentUser.get(), selectedUser);
    }

    @FXML
    private void onDeleteUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            usersStatusLabel.setText("Admin only");
            return;
        }

        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            usersStatusLabel.setText("Select a user first");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        boolean removed = adminService.deleteUser(currentUser.get(), selectedUser.getUsername());
        usersStatusLabel.setText(removed ? "Deleted" : "Not found");
        onLoadUsers();
    }

    @FXML
    private void onLoadCategories() {
        if (categoriesTable != null) {
            categoriesTable.getItems().clear();
        }
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            categoriesStatusLabel.setText("Admin only");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<String> categories = adminService.listCategories(currentUser.get());
        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        Map<String, Integer> countByCategory = new HashMap<>();
        for (Document document : documentRepository.findAll()) {
            countByCategory.merge(document.getCategory(), 1, Integer::sum);
        }

        List<CategoryRow> rows = new ArrayList<>();
        for (String category : categories) {
            rows.add(new CategoryRow(category, countByCategory.getOrDefault(category, 0)));
        }
        if (categoriesTable != null) {
            categoriesTable.getItems().setAll(rows);
        }
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

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New category");
        dialog.setHeaderText("Create a new category");
        dialog.setContentText("Category name:");
        Optional<String> nameInput = dialog.showAndWait();
        if (nameInput.isEmpty()) {
            categoriesStatusLabel.setText("Create canceled");
            return;
        }
        String name = nameInput.get();
        if (name == null || name.isBlank()) {
            categoriesStatusLabel.setText("Category required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        try {
            boolean added = adminService.addCategory(currentUser.get(), name);
            onLoadCategories();
            if (added) {
                categoriesStatusLabel.setText("Added");
            } else {
                categoriesStatusLabel.setText("Already exists");
                showCategoryWarning("Category already exists", "A category with this name already exists.");
            }
            refreshSummary();
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

        String oldName = getSelectedCategoryName();
        if (oldName == null || oldName.isBlank()) {
            categoriesStatusLabel.setText("Select a category first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Rename category");
        dialog.setHeaderText("Rename selected category");
        dialog.setContentText("New name:");
        Optional<String> newNameInput = dialog.showAndWait();
        if (newNameInput.isEmpty()) {
            categoriesStatusLabel.setText("Rename canceled");
            return;
        }
        String newName = newNameInput.get();
        if (newName == null || newName.isBlank()) {
            categoriesStatusLabel.setText("New name required");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<String> existingCategories = adminService.listCategories(currentUser.get());
        if (!oldName.equals(newName) && existingCategories.contains(newName)) {
            categoriesStatusLabel.setText("Already exists");
            showCategoryWarning("Category already exists", "A category with this name already exists.");
            return;
        }

        boolean renamed = adminService.renameCategory(currentUser.get(), oldName, newName);
        categoriesStatusLabel.setText(renamed ? "Renamed" : "Not found");
        onLoadCategories();
        onLoadDocuments();
    }

    @FXML
    private void onDeleteCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            categoriesStatusLabel.setText("Admin only");
            return;
        }

        String name = getSelectedCategoryName();
        if (name == null || name.isBlank()) {
            categoriesStatusLabel.setText("Select a category first");
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        boolean removed = adminService.deleteCategory(currentUser.get(), name);
        categoriesStatusLabel.setText(removed ? "Deleted" : "Not found");
        onLoadCategories();
        onLoadDocuments();
        onLoadWatchlist();
        refreshSummary();
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
        boolean canCreateDocuments = currentUser.isPresent() && currentUser.get().canManageDocuments();

        if (newDocumentButton != null) {
            newDocumentButton.setVisible(canCreateDocuments);
            newDocumentButton.setManaged(canCreateDocuments);
        }
        if (adminSettingsButton != null) {
            adminSettingsButton.setVisible(isAdmin);
            adminSettingsButton.setManaged(isAdmin);
        }
        if (!isAdmin) {
            showWorkspacePanel();
        }
    }

    @FXML
    private void onToggleAdminSettings() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            return;
        }

        if (adminSettingsVisible) {
            showWorkspacePanel();
        } else {
            showAdminSettingsPanel();
        }
    }

    private void showWorkspacePanel() {
        adminSettingsVisible = false;
        if (workspaceTabPane != null) {
            workspaceTabPane.setVisible(true);
            workspaceTabPane.setManaged(true);
        }
        if (adminTabPane != null) {
            adminTabPane.setVisible(false);
            adminTabPane.setManaged(false);
        }
        if (adminSettingsButton != null) {
            adminSettingsButton.setText("Admin settings");
        }
    }

    private void showAdminSettingsPanel() {
        adminSettingsVisible = true;
        if (workspaceTabPane != null) {
            workspaceTabPane.setVisible(false);
            workspaceTabPane.setManaged(false);
        }
        if (adminTabPane != null) {
            adminTabPane.setVisible(true);
            adminTabPane.setManaged(true);
        }
        if (adminSettingsButton != null) {
            adminSettingsButton.setText("Back to documents");
        }
        onLoadUsers();
        onLoadCategories();
        if (adminTabPane != null && usersTab != null) {
            adminTabPane.getSelectionModel().select(usersTab);
        }
    }

    @FXML
    private void onSaveAndExit() {
        AppState.getInstance().saveAll();
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private void updateResultsStatus(int count) {
        statusLabel.setText("Results: " + count);
    }

    private void showCategoryWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void populateSearchCategories() {
        if (searchCategoryCombo == null) {
            return;
        }

        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            searchCategoryCombo.getItems().setAll(ALL_CATEGORIES_OPTION);
            searchCategoryCombo.setValue(ALL_CATEGORIES_OPTION);
            return;
        }

        List<String> categories = new ArrayList<>();
        for (String category : AppState.getInstance().getCategoryRepository().findAll()) {
            if (currentUser.get().hasAccessToCategory(category)) {
                categories.add(category);
            }
        }
        categories.sort(Comparator.naturalOrder());

        String previousSelection = searchCategoryCombo.getValue();
        List<String> options = new ArrayList<>();
        options.add(ALL_CATEGORIES_OPTION);
        options.addAll(categories);
        searchCategoryCombo.getItems().setAll(options);

        if (previousSelection != null && options.contains(previousSelection)) {
            searchCategoryCombo.setValue(previousSelection);
        } else {
            searchCategoryCombo.setValue(ALL_CATEGORIES_OPTION);
        }
    }

    private List<Document> applyFollowingFilter(Optional<User> currentUser, List<Document> source) {
        if (followingOnlyCheck == null || !followingOnlyCheck.isSelected() || currentUser.isEmpty()) {
            return source;
        }

        User user = currentUser.get();
        List<Document> filtered = new ArrayList<>();
        for (Document document : source) {
            if (user.isFollowing(document.getId())) {
                filtered.add(document);
            }
        }
        return filtered;
    }

    private void showDocumentWindow(Document document, User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DocumentDialog.fxml"));
            Parent root = loader.load();

            DocumentDialogController controller = loader.getController();
            controller.setContext(document, currentUser);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(documentsTable.getScene().getWindow());
            dialogStage.setTitle(document.getTitle());
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                onLoadDocuments();
                onLoadWatchlist();
                refreshSummary();
            }
        } catch (IOException ex) {
            updateResultsStatus(documentsTable.getItems().size());
        }
    }

    private void showNewDocumentWindow(User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NewDocumentDialog.fxml"));
            Parent root = loader.load();

            NewDocumentDialogController controller = loader.getController();
            controller.setContext(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(documentsTable.getScene().getWindow());
            dialogStage.setTitle("New document");
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isCreated()) {
                onLoadDocuments();
                refreshSummary();
            }
        } catch (IOException ex) {
            updateResultsStatus(documentsTable.getItems().size());
        }
    }

    private void showAddUserWindow(User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            Parent root = loader.load();

            UserDialogController controller = loader.getController();
            controller.setAddContext(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usersTable.getScene().getWindow());
            dialogStage.setTitle("Add user");
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isCompleted()) {
                usersStatusLabel.setText("Added");
                onLoadUsers();
            }
        } catch (IOException ex) {
            usersStatusLabel.setText("Failed to open add user dialog");
        }
    }

    private void showEditUserWindow(User currentUser, User targetUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            Parent root = loader.load();

            UserDialogController controller = loader.getController();
            controller.setEditContext(currentUser, targetUser);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(usersTable.getScene().getWindow());
            dialogStage.setTitle("Edit user");
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isCompleted()) {
                usersStatusLabel.setText("Updated");
                onLoadUsers();
            }
        } catch (IOException ex) {
            usersStatusLabel.setText("Failed to open edit user dialog");
        }
    }

    private String getSelectedCategoryName() {
        if (categoriesTable == null) {
            return null;
        }
        CategoryRow selected = categoriesTable.getSelectionModel().getSelectedItem();
        return selected == null ? null : selected.getName();
    }

    private static final class CategoryRow {
        private final String name;
        private final int documentCount;

        private CategoryRow(String name, int documentCount) {
            this.name = name;
            this.documentCount = documentCount;
        }

        private String getName() {
            return name;
        }

        private int getDocumentCount() {
            return documentCount;
        }
    }
}
