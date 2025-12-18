package com.docflow.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.docflow.model.Document;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonStorage {

    private static final String DOCUMENTS_FILE = "medialab/documents.json";

    public static List<Document> loadDocuments() {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Document>>() {}.getType();
            return gson.fromJson(new FileReader(DOCUMENTS_FILE), listType);
        } catch (Exception e) {
            System.out.println("Error loading documents!");
            e.printStackTrace();
            return List.of();
        }
    }
}
