package com.docflow.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        com.docflow.AppState.getInstance().loadAll();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("MediaLab Documents");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        com.docflow.AppState.getInstance().saveAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
