package com.docflow.repository;

import com.docflow.model.Document;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DocumentRepository {

    private static final String DATA_FOLDER = "medialab";
    private static final String DOCUMENTS_FILE = "documents.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path documentsPath;
    private final Map<String, Document> documentsById = new LinkedHashMap<>();
    private final Type listType = new TypeToken<List<Document>>() {}.getType();

    public DocumentRepository() {
        this(Paths.get(DATA_FOLDER));
    }

    public DocumentRepository(Path baseDir) {
        this.documentsPath = baseDir.resolve(DOCUMENTS_FILE);
    }

    public void load() throws IOException {
        documentsById.clear();
        Files.createDirectories(documentsPath.getParent());

        if (!Files.exists(documentsPath) || Files.size(documentsPath) == 0) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(documentsPath, StandardCharsets.UTF_8)) {
            List<Document> docs = gson.fromJson(reader, listType);
            if (docs == null) {
                return;
            }
            for (Document doc : docs) {
                if (doc == null || doc.getId() == null || doc.getId().isBlank()) {
                    continue;
                }
                documentsById.put(doc.getId(), doc);
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(documentsPath.getParent());

        List<Document> docs = new ArrayList<>(documentsById.values());
        try (Writer writer = Files.newBufferedWriter(
                documentsPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            gson.toJson(docs, listType, writer);
        }
    }

    public List<Document> findAll() {
        return new ArrayList<>(documentsById.values());
    }

    public Optional<Document> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(documentsById.get(id));
    }

    public void add(Document document) {
        Objects.requireNonNull(document, "Document cannot be null");
        String id = document.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Document id cannot be empty");
        }
        if (documentsById.containsKey(id)) {
            throw new IllegalArgumentException("Document id already exists: " + id);
        }
        documentsById.put(id, document);
    }

    public void update(Document document) {
        Objects.requireNonNull(document, "Document cannot be null");
        String id = document.getId();
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Document id cannot be empty");
        }
        if (!documentsById.containsKey(id)) {
            throw new IllegalArgumentException("Document does not exist: " + id);
        }
        documentsById.put(id, document);
    }

    public boolean remove(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return documentsById.remove(id) != null;
    }
}
