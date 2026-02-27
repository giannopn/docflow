package com.docflow.controller;

import com.docflow.model.Document;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentInfoDialogController {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\R");

    @FXML private Label titleValueLabel;
    @FXML private Label authorValueLabel;
    @FXML private Label categoryValueLabel;
    @FXML private Label createdValueLabel;
    @FXML private Label modifiedValueLabel;
    @FXML private Label versionValueLabel;
    @FXML private Label charactersValueLabel;
    @FXML private Label wordsValueLabel;
    @FXML private Label paragraphsValueLabel;

    public void setDocument(Document document) {
        if (document == null) {
            return;
        }

        String content = document.getContent() == null ? "" : document.getContent();
        titleValueLabel.setText(document.getTitle());
        authorValueLabel.setText(document.getAuthor());
        categoryValueLabel.setText(document.getCategory());
        createdValueLabel.setText(document.getCreatedAt());
        modifiedValueLabel.setText(document.getModifiedAt());
        versionValueLabel.setText(String.valueOf(document.getVersion()));
        charactersValueLabel.setText(String.valueOf(content.length()));
        wordsValueLabel.setText(String.valueOf(countWords(content)));
        paragraphsValueLabel.setText(String.valueOf(countParagraphs(content)));
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) titleValueLabel.getScene().getWindow();
        stage.close();
    }

    private int countWords(String content) {
        String normalized = content == null ? "" : content.trim();
        return normalized.isEmpty() ? 0 : normalized.split("\\s+").length;
    }

    private int countParagraphs(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        int paragraphs = 1;
        Matcher matcher = NEW_LINE_PATTERN.matcher(content);
        while (matcher.find()) {
            paragraphs++;
        }
        return paragraphs;
    }
}
