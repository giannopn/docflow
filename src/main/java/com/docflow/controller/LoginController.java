package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.service.AuthService;
import com.docflow.service.WatchService;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        passwordVisibleField.visibleProperty().bind(showPasswordCheck.selectedProperty());
        passwordVisibleField.managedProperty().bind(showPasswordCheck.selectedProperty());
        passwordField.visibleProperty().bind(Bindings.not(showPasswordCheck.selectedProperty()));
        passwordField.managedProperty().bind(Bindings.not(showPasswordCheck.selectedProperty()));

        usernameField.textProperty().addListener((obs, oldValue, newValue) -> {
            updateLoginButtonState();
            statusLabel.setText("");
        });
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            updateLoginButtonState();
            statusLabel.setText("");
        });
        updateLoginButtonState();
    }

    @FXML
    private void onLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        AuthService authService = AppState.getInstance().getAuthService();
        Optional<User> user = authService.login(username, password);
        if (user.isEmpty()) {
            statusLabel.setText("Invalid credentials");
            return;
        }

        showUpdatedDocumentsPopup(user.get());
        openMainView();
    }

    private void showUpdatedDocumentsPopup(User user) {
        WatchService watchService = AppState.getInstance().getWatchService();
        List<Document> updated = watchService.listUpdatedSinceLastSeen(user);
        if (updated.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();
        for (Document doc : updated) {
            int oldVersion = user.getLastSeenVersion(doc.getId());
            int newVersion = doc.getVersion();
            message.append("- ")
                    .append(doc.getTitle())
                    .append(" (")
                    .append(doc.getAuthor())
                    .append(")")
                    .append(" [v")
                    .append(oldVersion)
                    .append(" -> v")
                    .append(newVersion)
                    .append("]")
                    .append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Updates");
        alert.setHeaderText("New versions are available for the following documents:");
        alert.setContentText(message.toString());
        alert.getDialogPane().setPrefSize(540, 360);
        alert.showAndWait();
    }

    private void openMainView() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    private void updateLoginButtonState() {
        if (loginButton == null) {
            return;
        }
        boolean disabled = usernameField.getText() == null || usernameField.getText().trim().isEmpty()
                || passwordField.getText() == null || passwordField.getText().trim().isEmpty();
        loginButton.setDisable(disabled);
    }
}
