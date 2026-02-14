package com.docflow;

import com.docflow.model.Document;
import com.docflow.repository.DocumentRepository;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("DocFlow backend test");

        AppState appState = AppState.getInstance();
        appState.loadAll();

        DocumentRepository documentRepository = appState.getDocumentRepository();
        List<Document> docs = documentRepository.findAll();

        System.out.println("Loaded: " + docs.size());
        for (Document d : docs) {
            System.out.println("- " + d);
        }

        appState.saveAll();
    }
}
