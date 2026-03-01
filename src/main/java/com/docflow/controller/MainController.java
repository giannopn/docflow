package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AdminService;
import com.docflow.service.AuthService;
import com.docflow.service.DocumentService;
import com.docflow.service.WatchService;
import com.docflow.ui.DialogStyler;
import com.docflow.ui.SceneStyler;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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
    private static final String DEFAULT_ADMIN_USERNAME = "medialab";
    private static final double BUTTON_ICON_SIZE = 16.0;
    private static final String ADMIN_SETTINGS_ICON = "/icons/user-cog.png";
    private static final String BACK_TO_DOCUMENTS_ICON = "/icons/table-properties.png";

    @FXML private TableView<Document> documentsTable;
    @FXML private TableColumn<Document, String> titleColumn;
    @FXML private TableColumn<Document, String> authorColumn;
    @FXML private TableColumn<Document, String> categoryColumn;
    @FXML private TableColumn<Document, String> createdAtColumn;
    @FXML private TableColumn<Document, String> modifiedAtColumn;
    @FXML private TableColumn<Document, Number> versionColumn;
    @FXML private TableColumn<Document, Document> updatesColumn;
    @FXML private Label statusLabel;
    @FXML private Label categoriesCountLabel;
    @FXML private Label documentsCountLabel;
    @FXML private Label usersCountLabel;
    @FXML private Label roleValueLabel;
    @FXML private Label accessibleCountLabel;
    @FXML private Label followingCountLabel;
    @FXML private Label updatesSummaryLabel;
    @FXML private TextField searchTitleField;
    @FXML private TextField searchAuthorField;
    @FXML private ComboBox<String> searchCategoryCombo;
    @FXML private CheckBox followingOnlyCheck;
    @FXML private Button newDocumentButton;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userFirstNameColumn;
    @FXML private TableColumn<User, String> userLastNameColumn;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableColumn<User, String> userCategoriesColumn;
    @FXML private TableView<CategoryRow> categoriesTable;
    @FXML private TableColumn<CategoryRow, String> categoryNameColumn;
    @FXML private TableColumn<CategoryRow, Number> categoryDocumentsColumn;
    @FXML private Label usersStatusLabel;
    @FXML private Label categoriesStatusLabel;
    @FXML private Button adminSettingsButton;
    @FXML private VBox workspacePane;
    @FXML private TabPane adminTabPane;
    @FXML private Tab usersTab;
    @FXML private Tab categoriesTab;

    private boolean adminSettingsVisible;

    @FXML
    private void initialize() {
        configureDocumentTable();
        configureUsersTable();
        configureCategoriesTable();
        if (followingOnlyCheck != null) {
            followingOnlyCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                onSearch();
                applyFollowingOnlySort(isSelected);
            });
        }
        adminSettingsVisible = false;
        configureTabAutoRefresh();
        applyRoleVisibility();
        showWorkspacePanel();
    }

    private void configureTabAutoRefresh() {
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
        titleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String title, boolean empty) {
                super.updateItem(title, empty);
                if (empty || title == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(title);
                setStyle("-fx-font-weight: 700;");
            }
        });
        authorColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getAuthor()));
        categoryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCategory()));
        createdAtColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCreatedAt()));
        modifiedAtColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getModifiedAt()));
        versionColumn.setCellValueFactory(cellData -> new ReadOnlyIntegerWrapper(cellData.getValue().getVersion()));
        updatesColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        updatesColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Document document, boolean empty) {
                super.updateItem(document, empty);
                if (empty || document == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                String value = formatUpdatesCellValue(document);
                setText(value);
                if (value.startsWith("Update:")) {
                    setStyle("-fx-text-fill: #b45309; -fx-font-weight: 700;");
                } else if (value.startsWith("Following")) {
                    setStyle("-fx-text-fill: #059669; -fx-font-weight: 700;");
                } else {
                    setStyle("-fx-text-fill: #6b7280; -fx-font-weight: 700;");
                }
            }
        });
        updatesColumn.setComparator(Comparator
                .comparingInt((Document document) -> getUpdatesPriority(document))
                .thenComparingInt(Document::getVersion)
                .thenComparing(Document::getTitle, String.CASE_INSENSITIVE_ORDER));

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

            MenuItem infoItem = new MenuItem("Info");
            infoItem.setOnAction(event -> {
                documentsTable.getSelectionModel().select(row.getItem());
                onShowDocumentInfo();
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
                refreshSummary();
            });

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                documentsTable.getSelectionModel().select(row.getItem());
                onDeleteDocument();
            });

            ContextMenu contextMenu = new ContextMenu(openItem, followItem, infoItem, deleteItem);
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

    private void applyFollowingOnlySort(boolean enabled) {
        if (documentsTable == null || updatesColumn == null) {
            return;
        }
        if (!enabled) {
            documentsTable.getSortOrder().clear();
            return;
        }
        updatesColumn.setSortType(TableColumn.SortType.ASCENDING);
        documentsTable.getSortOrder().setAll(updatesColumn);
        documentsTable.sort();
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
            if (cellData.getValue().canManageUsers()) {
                return new ReadOnlyStringWrapper(ALL_CATEGORIES_OPTION);
            }
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
            contextMenu.setOnShowing(event -> {
                User target = row.getItem();
                boolean actionBlocked = isDefaultAdminUser(target) || isCurrentAuthenticatedUser(target);
                editItem.setDisable(actionBlocked);
                deleteItem.setDisable(actionBlocked);
            });
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
        updateDocumentsResultsStatus(documents.size());
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
        if (followingOnlyCheck != null && followingOnlyCheck.isSelected()) {
            applyFollowingOnlySort(true);
        }
        updateDocumentsResultsStatus(results.size());
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

        WatchService watchService = AppState.getInstance().getWatchService();
        watchService.markSeen(currentUser.get(), selected.getId());
        documentsTable.refresh();
        refreshSummary();
        showDocumentWindow(selected, currentUser.get());
    }

    @FXML
    private void onShowDocumentInfo() {
        Document selected = documentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        showDocumentInfoWindow(selected);
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
        if (!confirmDocumentDeletion()) {
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        try {
            documentService.delete(currentUser.get(), selected.getId());
            onLoadDocuments();
        } catch (RuntimeException ex) {
            updateDocumentsResultsStatus(documentsTable.getItems().size());
        }
    }

    @FXML
    private void onLoadUsers() {
        if (usersTable != null) {
            usersTable.getItems().clear();
        }
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            updateUsersResultsStatus(0);
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<User> users = adminService.listUsers(currentUser.get());
        users.sort(Comparator
                .comparing((User user) -> !isDefaultAdminUser(user))
                .thenComparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
        if (usersTable != null) {
            usersTable.getItems().setAll(users);
        }
        updateUsersResultsStatus(users.size());
    }

    @FXML
    private void onAddUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            return;
        }

        showAddUserWindow(currentUser.get());
    }

    @FXML
    private void onEditUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            return;
        }

        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null
                || isDefaultAdminUser(selectedUser)
                || isCurrentAuthenticatedUser(selectedUser)) {
            return;
        }

        showEditUserWindow(currentUser.get(), selectedUser);
    }

    @FXML
    private void onDeleteUser() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageUsers()) {
            return;
        }

        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null
                || isDefaultAdminUser(selectedUser)
                || isCurrentAuthenticatedUser(selectedUser)) {
            return;
        }
        if (!confirmUserDeletion()) {
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        adminService.deleteUser(currentUser.get(), selectedUser.getUsername());
        onLoadUsers();
        refreshSummary();
    }

    private boolean isDefaultAdminUser(User user) {
        return user != null && DEFAULT_ADMIN_USERNAME.equalsIgnoreCase(user.getUsername());
    }

    private boolean isCurrentAuthenticatedUser(User user) {
        if (user == null) {
            return false;
        }
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        return currentUser.isPresent()
                && currentUser.get().getUsername().equalsIgnoreCase(user.getUsername());
    }

    @FXML
    private void onLoadCategories() {
        if (categoriesTable != null) {
            categoriesTable.getItems().clear();
        }
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            updateCategoriesResultsStatus(0);
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
        updateCategoriesResultsStatus(rows.size());
    }

    @FXML
    private void onAddCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New category");
        dialog.setHeaderText("Create a new category");
        dialog.setContentText("Category name:");
        DialogStyler.apply(dialog);
        Optional<String> nameInput = dialog.showAndWait();
        if (nameInput.isEmpty()) {
            return;
        }
        String name = nameInput.get();
        if (name == null || name.isBlank()) {
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        try {
            boolean added = adminService.addCategory(currentUser.get(), name);
            onLoadCategories();
            if (!added) {
                showCategoryWarning("Category already exists", "A category with this name already exists.");
            }
            refreshSummary();
        } catch (RuntimeException ex) {
            updateCategoriesResultsStatus(categoriesTable == null ? 0 : categoriesTable.getItems().size());
        }
    }

    @FXML
    private void onRenameCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            return;
        }

        String oldName = getSelectedCategoryName();
        if (oldName == null || oldName.isBlank()) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Rename category");
        dialog.setHeaderText("Rename selected category");
        dialog.setContentText("New name:");
        DialogStyler.apply(dialog);
        Optional<String> newNameInput = dialog.showAndWait();
        if (newNameInput.isEmpty()) {
            return;
        }
        String newName = newNameInput.get();
        if (newName == null || newName.isBlank()) {
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        List<String> existingCategories = adminService.listCategories(currentUser.get());
        if (!oldName.equals(newName) && existingCategories.contains(newName)) {
            showCategoryWarning("Category already exists", "A category with this name already exists.");
            return;
        }

        adminService.renameCategory(currentUser.get(), oldName, newName);
        onLoadCategories();
        onLoadDocuments();
    }

    @FXML
    private void onDeleteCategory() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageCategories()) {
            return;
        }

        String name = getSelectedCategoryName();
        if (name == null || name.isBlank()) {
            return;
        }
        if (!confirmCategoryDeletion()) {
            return;
        }

        AdminService adminService = AppState.getInstance().getAdminService();
        adminService.deleteCategory(currentUser.get(), name);
        onLoadCategories();
        onLoadDocuments();
    }

    private void refreshSummary() {
        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        int documentsCount = documentRepository.findAll().size();
        int categoriesCount = AppState.getInstance().getCategoryRepository().findAll().size();
        int usersCount = AppState.getInstance().getUserRepository().findAll().size();

        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        int followingCount = currentUser.map(user -> user.getFollowedDocuments().size()).orElse(0);
        int accessibleCount = currentUser
                .map(user -> AppState.getInstance().getDocumentService().listAccessible(user).size())
                .orElse(0);
        int updatesCount = currentUser
                .map(user -> AppState.getInstance().getWatchService().listUpdatedSinceLastSeen(user).size())
                .orElse(0);

        documentsCountLabel.setText(String.valueOf(documentsCount));
        categoriesCountLabel.setText(String.valueOf(categoriesCount));
        usersCountLabel.setText(String.valueOf(usersCount));
        roleValueLabel.setText(currentUser.map(user -> formatRoleLabel(user.getRole().name())).orElse("-"));
        accessibleCountLabel.setText(String.valueOf(accessibleCount));
        followingCountLabel.setText(String.valueOf(followingCount));
        updatesSummaryLabel.setText(String.valueOf(updatesCount));
    }

    private String formatRoleLabel(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "-";
        }

        String[] parts = roleName.toLowerCase().split("_");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                label.append(part.substring(1));
            }
        }
        return label.length() == 0 ? "-" : label.toString();
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
        if (workspacePane != null) {
            workspacePane.setVisible(true);
            workspacePane.setManaged(true);
        }
        if (adminTabPane != null) {
            adminTabPane.setVisible(false);
            adminTabPane.setManaged(false);
        }
        updateAdminSettingsButtonAppearance(false);
        onLoadDocuments();
    }

    private void showAdminSettingsPanel() {
        adminSettingsVisible = true;
        if (workspacePane != null) {
            workspacePane.setVisible(false);
            workspacePane.setManaged(false);
        }
        if (adminTabPane != null) {
            adminTabPane.setVisible(true);
            adminTabPane.setManaged(true);
        }
        updateAdminSettingsButtonAppearance(true);
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

    private void updateDocumentsResultsStatus(int count) {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        int accessibleCount = currentUser.isPresent()
                ? AppState.getInstance().getDocumentService().listAccessible(currentUser.get()).size()
                : AppState.getInstance().getDocumentRepository().findAll().size();

        if (count < accessibleCount) {
            statusLabel.setText(count + " of " + accessibleCount + " Documents");
            return;
        }
        statusLabel.setText(formatCountLabel(count, "Document", "Documents"));
    }

    private void updateUsersResultsStatus(int count) {
        usersStatusLabel.setText(formatCountLabel(count, "User", "Users"));
    }

    private void updateCategoriesResultsStatus(int count) {
        categoriesStatusLabel.setText(formatCountLabel(count, "Category", "Categories"));
    }

    private String formatCountLabel(int count, String singular, String plural) {
        return count + " " + (count == 1 ? singular : plural);
    }

    private void updateAdminSettingsButtonAppearance(boolean adminSettingsMode) {
        if (adminSettingsButton == null) {
            return;
        }
        adminSettingsButton.setText(adminSettingsMode ? "Documents" : "Admin settings");
        adminSettingsButton.setGraphic(createButtonIcon(
                adminSettingsMode ? BACK_TO_DOCUMENTS_ICON : ADMIN_SETTINGS_ICON
        ));
        adminSettingsButton.setGraphicTextGap(6);
    }

    private ImageView createButtonIcon(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        java.net.URL iconUrl = getClass().getResource(resourcePath);
        if (iconUrl == null) {
            return null;
        }
        ImageView icon = new ImageView(new Image(iconUrl.toExternalForm()));
        icon.setFitHeight(BUTTON_ICON_SIZE);
        icon.setPreserveRatio(true);
        return icon;
    }

    private void showCategoryWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        DialogStyler.apply(alert);
        alert.showAndWait();
    }

    private boolean confirmDocumentDeletion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm delete");
        alert.setHeaderText("Delete selected document?");
        alert.setContentText("This action cannot be undone.");
        DialogStyler.apply(alert);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private boolean confirmCategoryDeletion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm delete");
        alert.setHeaderText("Delete selected category?");
        alert.setContentText("All documents in this category will also be deleted.");
        DialogStyler.apply(alert);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private boolean confirmUserDeletion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm delete");
        alert.setHeaderText("Delete selected user?");
        alert.setContentText("Documents created by this user will stay in the system.");
        DialogStyler.apply(alert);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
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

    private String formatUpdatesCellValue(Document document) {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return "Not followed";
        }

        User user = currentUser.get();
        if (!user.isFollowing(document.getId())) {
            return "Not followed";
        }

        int oldVersion = user.getLastSeenVersion(document.getId());
        int newVersion = document.getVersion();
        if (newVersion > oldVersion) {
            return "Update: v" + oldVersion + " -> v" + newVersion;
        }
        return "Following: No updates";
    }

    private int getUpdatesPriority(Document document) {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return 2;
        }

        User user = currentUser.get();
        if (!user.isFollowing(document.getId())) {
            return 2;
        }

        int oldVersion = user.getLastSeenVersion(document.getId());
        int newVersion = document.getVersion();
        return newVersion > oldVersion ? 0 : 1;
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
            SceneStyler.apply(scene);
            dialogStage.setScene(scene);
            dialogStage.setOnCloseRequest(event -> {
                if (!controller.canCloseDialog()) {
                    event.consume();
                }
            });
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                onLoadDocuments();
            }
        } catch (IOException ex) {
            updateDocumentsResultsStatus(documentsTable.getItems().size());
        }
    }

    private void showDocumentInfoWindow(Document document) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DocumentInfoDialog.fxml"));
            Parent root = loader.load();

            DocumentInfoDialogController controller = loader.getController();
            controller.setDocument(document);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(documentsTable.getScene().getWindow());
            dialogStage.setTitle(document.getTitle() + " - Info");
            Scene scene = new Scene(root);
            SceneStyler.apply(scene);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (IOException ex) {
            updateDocumentsResultsStatus(documentsTable.getItems().size());
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
            SceneStyler.apply(scene);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isCreated()) {
                onLoadDocuments();
            }
        } catch (IOException ex) {
            updateDocumentsResultsStatus(documentsTable.getItems().size());
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
            SceneStyler.apply(scene);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isCompleted()) {
                onLoadUsers();
                refreshSummary();
            }
        } catch (IOException ex) {
            updateUsersResultsStatus(usersTable == null ? 0 : usersTable.getItems().size());
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
            SceneStyler.apply(scene);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isCompleted()) {
                onLoadUsers();
            }
        } catch (IOException ex) {
            updateUsersResultsStatus(usersTable == null ? 0 : usersTable.getItems().size());
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
