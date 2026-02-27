package com.docflow.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.net.URL;

public final class DialogStyler {
    private static final String STYLESHEET_PATH = "/css/style.css";
    private static final String CANCEL_BUTTON_CLASS = "admin-add-button";
    private static final String OK_BUTTON_CLASS = "dialog-ok-button";
    private static final String INPUT_CLASS = "admin-add-input";

    private DialogStyler() {
    }

    public static void apply(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }
        DialogPane dialogPane = dialog.getDialogPane();
        if (dialogPane == null) {
            return;
        }

        URL stylesheetUrl = DialogStyler.class.getResource(STYLESHEET_PATH);
        if (stylesheetUrl != null) {
            String stylesheet = stylesheetUrl.toExternalForm();
            if (!dialogPane.getStylesheets().contains(stylesheet)) {
                dialogPane.getStylesheets().add(stylesheet);
            }
        }

        dialogPane.getButtonTypes().forEach(buttonType -> {
            Node node = dialogPane.lookupButton(buttonType);
            if (node instanceof Button button) {
                styleButton(button, buttonType.getButtonData());
            }
        });

        if (dialog instanceof TextInputDialog textInputDialog) {
            TextField editor = textInputDialog.getEditor();
            if (editor != null && !editor.getStyleClass().contains(INPUT_CLASS)) {
                editor.getStyleClass().add(INPUT_CLASS);
            }
        }
    }

    private static void styleButton(Button button, ButtonBar.ButtonData buttonData) {
        button.getStyleClass().remove(CANCEL_BUTTON_CLASS);
        button.getStyleClass().remove(OK_BUTTON_CLASS);

        if (buttonData == null) {
            return;
        }
        if (buttonData.isCancelButton()) {
            button.getStyleClass().add(CANCEL_BUTTON_CLASS);
            return;
        }
        if (isSubmissionButton(buttonData)) {
            button.getStyleClass().add(OK_BUTTON_CLASS);
        }
    }

    private static boolean isSubmissionButton(ButtonBar.ButtonData buttonData) {
        return buttonData == ButtonBar.ButtonData.OK_DONE
                || buttonData == ButtonBar.ButtonData.YES
                || buttonData == ButtonBar.ButtonData.FINISH
                || buttonData == ButtonBar.ButtonData.APPLY
                || buttonData == ButtonBar.ButtonData.NEXT_FORWARD;
    }
}
