package com.docflow.controller;

import com.docflow.AppState;
import com.docflow.model.Document;
import com.docflow.model.User;
import com.docflow.service.AuthService;
import com.docflow.service.WatchService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
    @FXML private Label statusLabel;

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
            message.append("- ").append(doc.getTitle()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Updated Documents");
        alert.setHeaderText("New versions available");
        alert.setContentText(message.toString());
        alert.showAndWait();

        watchService.markAllSeen(user);
    }

    private void openMainView() throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
