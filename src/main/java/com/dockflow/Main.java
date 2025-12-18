package com.dockflow;

import com.dockflow.model.Document;
import com.dockflow.storage.JsonStorage;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.out.println("📂 Loading documents...");

        List<Document> documents = JsonStorage.loadDocuments();

        System.out.println("Found " + documents.size() + " documents:\n");

        for (Document doc : documents) {
            System.out.println("- " + doc);
        }
    }
}
