package com.docflow.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.docflow.model.Document;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;

public class JsonStorage {

    private static final String DOCUMENTS_FILE = "medialab/documents.json";

    public static List<Document> loadDocuments() {
        try (FileReader reader = new FileReader(DOCUMENTS_FILE)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Document>>() {}.getType();
            List<Document> documents = gson.fromJson(reader, listType);
            if (documents == null) {
                return List.of();
            }
            return documents;
        } catch (Exception e) {
            System.out.println("Error loading documents!");
            e.printStackTrace();
            return List.of();
        }
    }
}
