package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import com.docflow.service.AdminService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AddUserDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<UserRole> roleSelector;
    @FXML private ListView<String> categoriesList;
    @FXML private Label statusLabel;
    @FXML private Button addButton;

    private User currentUser;
    private AdminService adminService;
    private boolean created;

    @FXML
    private void initialize() {
        roleSelector.getItems().setAll(UserRole.values());
        categoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        firstNameField.textProperty().addListener((obs, oldValue, newValue) -> updateAddButtonState());
        lastNameField.textProperty().addListener((obs, oldValue, newValue) -> updateAddButtonState());
        usernameField.textProperty().addListener((obs, oldValue, newValue) -> updateAddButtonState());
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> updateAddButtonState());
        roleSelector.valueProperty().addListener((obs, oldValue, newValue) -> updateAddButtonState());
        categoriesList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> updateAddButtonState());
    }

    public void setContext(User currentUser) {
        this.currentUser = currentUser;
        this.adminService = AppState.getInstance().getAdminService();
        this.created = false;
        statusLabel.setText("");

        List<String> categories = new ArrayList<>(AppState.getInstance().getCategoryRepository().findAll());
        categories.sort(Comparator.naturalOrder());
        categoriesList.getItems().setAll(categories);

        roleSelector.getSelectionModel().select(UserRole.SIMPLE_USER);
        updateAddButtonState();
    }

    public boolean isCreated() {
        return created;
    }

    @FXML
    private void onAdd() {
        if (currentUser == null) {
            return;
        }

        String firstName = normalize(firstNameField.getText());
        String lastName = normalize(lastNameField.getText());
        String username = normalize(usernameField.getText());
        String password = normalize(passwordField.getText());
        UserRole role = roleSelector.getValue();
        Set<String> categories = new LinkedHashSet<>(categoriesList.getSelectionModel().getSelectedItems());

        try {
            adminService.addUser(currentUser, firstName, lastName, username, password, role, categories);
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

    private void updateAddButtonState() {
        String firstName = normalize(firstNameField.getText());
        String lastName = normalize(lastNameField.getText());
        String username = normalize(usernameField.getText());
        String password = normalize(passwordField.getText());
        UserRole role = roleSelector.getValue();
        boolean hasCategories = !categoriesList.getSelectionModel().getSelectedItems().isEmpty();

        boolean valid = !firstName.isBlank()
                && !lastName.isBlank()
                && !username.isBlank()
                && !password.isBlank()
                && role != null
                && (role == UserRole.ADMIN || hasCategories);
        addButton.setDisable(!valid);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private void closeWindow() {
        Stage stage = (Stage) addButton.getScene().getWindow();
        stage.close();
    }
}

