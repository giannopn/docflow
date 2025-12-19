package com.docflow.repository;

import com.docflow.model.Admin;
import com.docflow.model.Author;
import com.docflow.model.SimpleUser;
import com.docflow.model.User;
import com.google.gson.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Repository responsible for loading and saving users to JSON files.
 * <p>
 * Data folder: "medialab"
 * File: "users.json"
 */
public class UserRepository {

    private static final String DATA_FOLDER = "medialab";
    private static final String USERS_FILE = "users.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path usersPath;

    private final Map<String, User> usersByUsername = new LinkedHashMap<>();

    /**
     * Creates a repository using the default data folder ("medialab") in the working directory.
     */
    public UserRepository() {
        this(Paths.get(DATA_FOLDER));
    }

    /**
     * Creates a repository using a custom base directory.
     *
     * @param baseDir base directory where JSON files are stored
     */
    public UserRepository(Path baseDir) {
        this.usersPath = baseDir.resolve(USERS_FILE);
    }

    /**
     * Loads users from JSON into memory. If the file does not exist, it is created.
     * Ensures that the default admin user exists (username: "medialab", password: "medialab_2025").
     *
     * @throws IOException if file operations fail
     */
    public void load() throws IOException {
        usersByUsername.clear();

        Files.createDirectories(usersPath.getParent());

        if (!Files.exists(usersPath)) {
            ensureDefaultAdmin();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(usersPath, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);

            if (root == null || root.isJsonNull()) {
                ensureDefaultAdmin();
                save();
                return;
            }

            if (!root.isJsonArray()) {
                throw new IllegalStateException("Invalid users.json format: expected a JSON array");
            }

            JsonArray arr = root.getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;

                JsonObject obj = el.getAsJsonObject();
                User user = deserializeUser(obj);
                if (user == null) continue;

                usersByUsername.put(user.getUsername(), user);
            }
        }

        ensureDefaultAdmin();
    }

    /**
     * Saves all in-memory users to JSON.
     *
     * @throws IOException if file operations fail
     */
    public void save() throws IOException {
        Files.createDirectories(usersPath.getParent());

        JsonArray arr = new JsonArray();
        for (User user : usersByUsername.values()) {
            JsonObject obj = serializeUser(user);
            arr.add(obj);
        }

        try (Writer writer = Files.newBufferedWriter(
                usersPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            gson.toJson(arr, writer);
        }
    }

    /**
     * Returns all users as a list (copy).
     *
     * @return list of users
     */
    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return optional user
     */
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(usersByUsername.get(username));
    }

    /**
     * Adds a new user. Username must be unique.
     *
     * @param user the user to add
     */
    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        String username = user.getUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
