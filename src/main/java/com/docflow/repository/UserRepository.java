package com.docflow.repository;

import com.docflow.model.Admin;
import com.docflow.model.Author;
import com.docflow.model.SimpleUser;
import com.docflow.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Repository responsible for loading and saving users to JSON files.
 */
public class UserRepository {

    private static final String DATA_FOLDER = "medialab";
    private static final String USERS_FILE = "users.json";

    private static final String DEFAULT_ADMIN_FIRST_NAME = "Media";
    private static final String DEFAULT_ADMIN_LAST_NAME = "Lab";
    private static final String DEFAULT_ADMIN_USERNAME = "medialab";
    private static final String DEFAULT_ADMIN_PASSWORD = "medialab_2025";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path usersPath;
    private final Map<String, User> usersByUsername = new LinkedHashMap<>();

    public UserRepository() {
        this(Paths.get(DATA_FOLDER));
    }

    public UserRepository(Path baseDir) {
        this.usersPath = baseDir.resolve(USERS_FILE);
    }

    public void load() throws IOException {
        usersByUsername.clear();
        Files.createDirectories(usersPath.getParent());

        if (!Files.exists(usersPath) || Files.size(usersPath) == 0) {
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
                throw new IllegalStateException("Invalid users.json format: expected array");
            }

            for (JsonElement element : root.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                User user = deserializeUser(element.getAsJsonObject());
                usersByUsername.put(user.getUsername(), user);
            }
        }

        ensureDefaultAdmin();
    }

    public void save() throws IOException {
        Files.createDirectories(usersPath.getParent());

        JsonArray array = new JsonArray();
        for (User user : usersByUsername.values()) {
            array.add(serializeUser(user));
        }

        try (Writer writer = Files.newBufferedWriter(
                usersPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            gson.toJson(array, writer);
        }
    }

    public List<User> findAll() {
        return new ArrayList<>(usersByUsername.values());
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        String username = user.getUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        usersByUsername.put(username, user);
    }

    public boolean remove(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        if (DEFAULT_ADMIN_USERNAME.equals(username)) {
            return false;
        }
        return usersByUsername.remove(username) != null;
    }

    public void update(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        if (!usersByUsername.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("User does not exist: " + user.getUsername());
        }
        usersByUsername.put(user.getUsername(), user);
    }

    private void ensureDefaultAdmin() {
        if (usersByUsername.containsKey(DEFAULT_ADMIN_USERNAME)) {
            return;
        }

        Set<String> adminCategories = new LinkedHashSet<>();
        adminCategories.add("Multimedia");
        adminCategories.add("Programming");

        Admin admin = new Admin(
                DEFAULT_ADMIN_FIRST_NAME,
                DEFAULT_ADMIN_LAST_NAME,
                DEFAULT_ADMIN_USERNAME,
                DEFAULT_ADMIN_PASSWORD,
                adminCategories
        );

        usersByUsername.put(admin.getUsername(), admin);
    }

    private JsonObject serializeUser(User user) {
        JsonObject obj = new JsonObject();
        obj.addProperty("firstName", user.getFirstName());
        obj.addProperty("lastName", user.getLastName());
        obj.addProperty("username", user.getUsername());
        obj.addProperty("password", user.getPassword());
        obj.addProperty("role", extractRole(user));

        JsonArray categories = new JsonArray();
        for (String category : user.getAllowedCategories()) {
            categories.add(category);
        }
        obj.add("allowedCategories", categories);

        JsonArray followed = new JsonArray();
        for (String documentId : user.getFollowedDocuments()) {
            followed.add(documentId);
        }
        obj.add("followedDocuments", followed);

        return obj;
    }

    private User deserializeUser(JsonObject obj) {
        String firstName = getAsString(obj, "firstName", "");
        String lastName = getAsString(obj, "lastName", "");
        String username = getAsString(obj, "username", "");
        String password = getAsString(obj, "password", "");
        String role = getAsString(obj, "role", "SIMPLE_USER");

        Set<String> allowedCategories = getAsStringSet(obj, "allowedCategories");
        Set<String> followedDocuments = getAsStringSet(obj, "followedDocuments");

        User user;
        switch (role) {
            case "ADMIN":
                user = new Admin(firstName, lastName, username, password, allowedCategories);
                break;
            case "AUTHOR":
                user = new Author(firstName, lastName, username, password, allowedCategories);
                break;
            default:
                user = new SimpleUser(firstName, lastName, username, password, allowedCategories);
                break;
        }

        user.setFollowedDocuments(followedDocuments);
        return user;
    }

    private String extractRole(User user) {
        if (user instanceof Admin) {
            return "ADMIN";
        }
        if (user instanceof Author) {
            return "AUTHOR";
        }
        return "SIMPLE_USER";
    }

    private String getAsString(JsonObject obj, String key, String defaultValue) {
        JsonElement value = obj.get(key);
        if (value == null || value.isJsonNull()) {
            return defaultValue;
        }
        return value.getAsString();
    }

    private Set<String> getAsStringSet(JsonObject obj, String key) {
        Set<String> result = new LinkedHashSet<>();
        JsonElement value = obj.get(key);
        if (value == null || !value.isJsonArray()) {
            return result;
        }

        for (JsonElement element : value.getAsJsonArray()) {
            if (element.isJsonNull()) {
                continue;
            }
            result.add(element.getAsString());
        }
        return result;
    }
}
