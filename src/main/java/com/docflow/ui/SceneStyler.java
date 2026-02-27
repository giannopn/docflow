package com.docflow.ui;

import javafx.scene.Scene;

import java.net.URL;

public final class SceneStyler {
    private static final String STYLESHEET_PATH = "/css/style.css";

    private SceneStyler() {
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }
        URL stylesheetUrl = SceneStyler.class.getResource(STYLESHEET_PATH);
        if (stylesheetUrl == null) {
            return;
        }
        String stylesheet = stylesheetUrl.toExternalForm();
        if (!scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
        }
    }
}
