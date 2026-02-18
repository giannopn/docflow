package com.docflow.repository;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CategoryRepository {

    private static final String DATA_FOLDER = "medialab";
    private static final String CATEGORIES_FILE = "categories.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path categoriesPath;
    private final Set<String> categories = new LinkedHashSet<>();
    private final Type listType = new TypeToken<List<String>>() {}.getType();

    public CategoryRepository() {
        this(Paths.get(DATA_FOLDER));
    }

    public CategoryRepository(Path baseDir) {
        this.categoriesPath = baseDir.resolve(CATEGORIES_FILE);
    }

    public void load() throws IOException {
        categories.clear();

        if (!Files.exists(categoriesPath) || Files.size(categoriesPath) == 0) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(categoriesPath, StandardCharsets.UTF_8)) {
            List<String> loaded = gson.fromJson(reader, listType);
            if (loaded == null) {
                return;
            }
            for (String category : loaded) {
                if (category == null || category.isBlank()) {
                    continue;
                }
                categories.add(category);
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(categoriesPath.getParent());

        List<String> list = new ArrayList<>(categories);
        try (Writer writer = Files.newBufferedWriter(
                categoriesPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            gson.toJson(list, listType, writer);
        }
    }

    public List<String> findAll() {
        return new ArrayList<>(categories);
    }

    public boolean add(String category) {
        Objects.requireNonNull(category, "Category cannot be null");
        if (category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        return categories.add(category);
    }

    public boolean remove(String category) {
        if (category == null || category.isBlank()) {
            return false;
        }
        return categories.remove(category);
    }

    public boolean rename(String oldName, String newName) {
        if (oldName == null || oldName.isBlank() || newName == null || newName.isBlank()) {
            return false;
        }
        if (!categories.contains(oldName)) {
            return false;
        }
        categories.remove(oldName);
        categories.add(newName);
        return true;
    }
}
