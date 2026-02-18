package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import com.docflow.service.AdminService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDialogController {

    private enum Mode {
        ADD,
        EDIT
    }

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private ComboBox<UserRole> roleSelector;
    @FXML private ListView<String> categoriesList;
    @FXML private Label statusLabel;
    @FXML private Button actionButton;

    private final Map<String, BooleanProperty> categoryChecks = new LinkedHashMap<>();
    private User currentAdmin;
    private User targetUser;
    private AdminService adminService;
    private Mode mode = Mode.ADD;
    private boolean completed;

    @FXML
    private void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisibleField.visibleProperty().bind(showPasswordCheck.selectedProperty());
        passwordVisibleField.managedProperty().bind(showPasswordCheck.selectedProperty());
        passwordField.visibleProperty().bind(Bindings.not(showPasswordCheck.selectedProperty()));
        passwordField.managedProperty().bind(Bindings.not(showPasswordCheck.selectedProperty()));

        roleSelector.getItems().setAll(UserRole.values());
        categoriesList.setCellFactory(CheckBoxListCell.forListView(item -> categoryChecks.get(item)));

        firstNameField.textProperty().addListener((obs, oldValue, newValue) -> updateActionButtonState());
        lastNameField.textProperty().addListener((obs, oldValue, newValue) -> updateActionButtonState());
        usernameField.textProperty().addListener((obs, oldValue, newValue) -> updateActionButtonState());
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> updateActionButtonState());
        roleSelector.valueProperty().addListener((obs, oldValue, newValue) -> updateActionButtonState());
    }

    public void setAddContext(User currentAdmin) {
        this.mode = Mode.ADD;
        this.currentAdmin = currentAdmin;
        this.targetUser = null;
        this.adminService = AppState.getInstance().getAdminService();
        this.completed = false;

        configureCategoryChecks(Set.of());
        firstNameField.setText("");
        lastNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheck.setSelected(false);
        usernameField.setDisable(false);
        passwordField.setDisable(false);
        passwordVisibleField.setDisable(false);
        showPasswordCheck.setDisable(false);
        roleSelector.setDisable(false);
        roleSelector.getSelectionModel().select(UserRole.SIMPLE_USER);
        actionButton.setText("Add user");
        statusLabel.setText("");

        updateActionButtonState();
    }

    public void setEditContext(User currentAdmin, User targetUser) {
        this.mode = Mode.EDIT;
        this.currentAdmin = currentAdmin;
        this.targetUser = targetUser;
        this.adminService = AppState.getInstance().getAdminService();
        this.completed = false;

        configureCategoryChecks(targetUser.getAllowedCategories());
        firstNameField.setText(targetUser.getFirstName());
        lastNameField.setText(targetUser.getLastName());
        usernameField.setText(targetUser.getUsername());
        passwordField.setText(targetUser.getPassword());
        showPasswordCheck.setSelected(false);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        passwordVisibleField.setDisable(true);
        showPasswordCheck.setDisable(false);
        roleSelector.getSelectionModel().select(targetUser.getRole());
        roleSelector.setDisable(true);
        actionButton.setText("Save changes");
        statusLabel.setText("");

        updateActionButtonState();
    }

    public boolean isCompleted() {
        return completed;
    }

    @FXML
    private void onSubmit() {
        if (currentAdmin == null) {
            return;
        }

        String firstName = normalize(firstNameField.getText());
        String lastName = normalize(lastNameField.getText());
        String username = normalize(usernameField.getText());
        String password = normalize(passwordField.getText());
        UserRole role = roleSelector.getValue();
        Set<String> categories = categoryChecks.entrySet().stream()
                .filter(entry -> entry.getValue().get())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        try {
            if (mode == Mode.ADD) {
                adminService.addUser(currentAdmin, firstName, lastName, username, password, role, categories);
            } else {
                adminService.updateUserProfile(currentAdmin, username, firstName, lastName, categories);
            }
            completed = true;
            closeWindow();
        } catch (RuntimeException ex) {
            if (mode == Mode.ADD && isDuplicateUsernameError(ex)) {
                showWarning(
                        "Username already exists",
                        "This username is already used. Please choose a different username."
                );
                usernameField.requestFocus();
                usernameField.selectAll();
                return;
            }
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void configureCategoryChecks(Set<String> selectedCategories) {
        categoryChecks.clear();
        List<String> categories = new ArrayList<>(AppState.getInstance().getCategoryRepository().findAll());
        categories.sort(Comparator.naturalOrder());
        for (String category : categories) {
            BooleanProperty selected = new SimpleBooleanProperty(selectedCategories.contains(category));
            selected.addListener((obs, oldValue, newValue) -> updateActionButtonState());
            categoryChecks.put(category, selected);
        }
        categoriesList.getItems().setAll(categories);
    }

    private void updateActionButtonState() {
        String firstName = normalize(firstNameField.getText());
        String lastName = normalize(lastNameField.getText());
        String username = normalize(usernameField.getText());
        String password = normalize(passwordField.getText());
        UserRole role = roleSelector.getValue();
        boolean hasCategories = categoryChecks.values().stream().anyMatch(BooleanProperty::get);

        boolean valid;
        if (mode == Mode.ADD) {
            valid = !firstName.isBlank()
                    && !lastName.isBlank()
                    && !username.isBlank()
                    && !password.isBlank()
                    && role != null
                    && (role == UserRole.ADMIN || hasCategories);
        } else {
            UserRole targetRole = targetUser == null ? role : targetUser.getRole();
            valid = !firstName.isBlank()
                    && !lastName.isBlank()
                    && targetRole != null
                    && (targetRole == UserRole.ADMIN || hasCategories);
        }
        actionButton.setDisable(!valid);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private void closeWindow() {
        Stage stage = (Stage) actionButton.getScene().getWindow();
        stage.close();
    }

    private boolean isDuplicateUsernameError(RuntimeException ex) {
        String message = ex.getMessage();
        return message != null && message.toLowerCase().contains("username already exists");
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        Stage owner = (Stage) actionButton.getScene().getWindow();
        alert.initOwner(owner);
        alert.showAndWait();
    }
}
