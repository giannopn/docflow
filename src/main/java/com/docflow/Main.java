package com.docflow;

import com.docflow.model.Document;
import com.docflow.repository.JsonStorage;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("DocFlow backend test");

        List<Document> docs = JsonStorage.loadDocuments();

        System.out.println("Loaded: " + docs.size());
        for (Document d : docs) {
            System.out.println("- " + d);
        }
    }
}
