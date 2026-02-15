/* Ο controller ειναι ενδιαμεσα απο το backend και το frontend.
Βλεπει τι κουμπια πατησε ο χρηστης και καλει το backend */

package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import com.docflow.repository.DocumentRepository;
import com.docflow.service.AdminService;
import com.docflow.service.AuthService;
import com.docflow.service.DocumentService;
import com.docflow.service.WatchService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

public class MainController {

    @FXML private ListView<String> documentsList;
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
        refreshSummary();
        applyRoleVisibility();
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

    @FXML
    private void onFollowSelectedDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty()) {
            return;
        }

        String selected = documentsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        WatchService watchService = AppState.getInstance().getWatchService();
        List<Document> accessible = documentService.listAccessible(currentUser.get());
        for (Document doc : accessible) {
            if (selected.equals(doc.toString())) {
                watchService.follow(currentUser.get(), doc.getId());
                break;
            }
        }

        refreshSummary();
    }

    @FXML
    private void onSearch() {
        documentsList.getItems().clear();
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
        for (Document doc : results) {
            documentsList.getItems().add(doc.toString());
        }
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
    private void onEditDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageDocuments()) {
            statusLabel.setText("Author/Admin only");
            return;
        }

        String selected = documentsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            statusLabel.setText("Select a document");
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        List<Document> accessible = documentService.listAccessible(currentUser.get());
        Document target = null;
        for (Document doc : accessible) {
            if (selected.equals(doc.toString())) {
                target = doc;
                break;
            }
        }
        if (target == null) {
            statusLabel.setText("Document not found");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(target.getContent());
        dialog.setTitle("Edit Document");
        dialog.setHeaderText("Update content");
        dialog.setContentText("Content:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            documentService.updateContent(currentUser.get(), target.getId(), result.get());
            statusLabel.setText("Updated");
            onLoadDocuments();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onDeleteDocument() {
        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isEmpty() || !currentUser.get().canManageDocuments()) {
            statusLabel.setText("Author/Admin only");
            return;
        }

        String selected = documentsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            statusLabel.setText("Select a document");
            return;
        }

        DocumentService documentService = AppState.getInstance().getDocumentService();
        List<Document> accessible = documentService.listAccessible(currentUser.get());
        Document target = null;
        for (Document doc : accessible) {
            if (selected.equals(doc.toString())) {
                target = doc;
                break;
            }
        }
        if (target == null) {
            statusLabel.setText("Document not found");
            return;
        }

        try {
            boolean removed = documentService.delete(currentUser.get(), target.getId());
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

        String selected = documentsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isBlank()) {
            return;
        }

        DocumentRepository documentRepository = AppState.getInstance().getDocumentRepository();
        DocumentService documentService = AppState.getInstance().getDocumentService();
        WatchService watchService = AppState.getInstance().getWatchService();

        List<Document> accessible = documentService.listAccessible(currentUser.get());
        for (Document doc : accessible) {
            if (selected.equals(doc.toString())) {
                watchService.follow(currentUser.get(), doc.getId());
                break;
            }
        }

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
}
