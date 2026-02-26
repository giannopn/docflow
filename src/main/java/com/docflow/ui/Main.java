package com.docflow.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import com.docflow.service.AuthService;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final boolean AUTO_LOGIN_AS_ADMIN = false;

    @Override
    public void start(Stage stage) throws Exception {
        com.docflow.AppState.getInstance().loadAll();

        String view = "/fxml/LoginView.fxml";
        if (AUTO_LOGIN_AS_ADMIN) {
            AuthService authService = com.docflow.AppState.getInstance().getAuthService();
            boolean loggedIn = authService.login("medialab", "medialab_2025").isPresent();
            if (loggedIn) {
                view = "/fxml/MainView.fxml";
            }
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
        Scene scene = new Scene(loader.load());

        stage.setTitle("MediaLab Documents");
        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        com.docflow.AppState.getInstance().saveAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
