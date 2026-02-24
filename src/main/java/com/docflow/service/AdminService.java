package com.docflow.service;

import com.docflow.model.Admin;
import com.docflow.model.Author;
import com.docflow.model.SimpleUser;
import com.docflow.model.User;
import com.docflow.model.UserRole;
import com.docflow.repository.CategoryRepository;
import com.docflow.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AdminService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentService documentService;

    public AdminService(UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        DocumentService documentService) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.documentService = documentService;
    }

    public List<User> listUsers(User admin) {
        requireAdmin(admin);
        return userRepository.findAll();
    }

    public void addUser(User admin,
                        String firstName,
                        String lastName,
                        String username,
                        String password,
                        UserRole role,
                        Set<String> allowedCategories) {
        requireAdmin(admin);
        validateUserInput(firstName, lastName, username, password, role, allowedCategories);
        userRepository.add(createUser(firstName, lastName, username, password, role, allowedCategories));
    }

    public boolean deleteUser(User admin, String username) {
        requireAdmin(admin);
        return userRepository.remove(username);
    }

    public void updateUserProfile(User admin,
                                  String username,
                                  String newFirstName,
                                  String newLastName,
                                  Set<String> newAllowedCategories) {
        requireAdmin(admin);
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        if (newFirstName == null || newFirstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (newLastName == null || newLastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }

        User user = existing.get();
        if (user.getRole() != UserRole.ADMIN
                && (newAllowedCategories == null || newAllowedCategories.isEmpty())) {
            throw new IllegalArgumentException("At least one category is required");
        }

        user.setFirstName(newFirstName.trim());
        user.setLastName(newLastName.trim());
        user.setAllowedCategories(newAllowedCategories == null ? Set.of() : newAllowedCategories);
        userRepository.update(user);
    }

    public List<String> listCategories(User admin) {
        requireAdmin(admin);
        return categoryRepository.findAll();
    }

    public boolean addCategory(User admin, String category) {
        requireAdmin(admin);
        return categoryRepository.add(category);
    }

    public boolean renameCategory(User admin, String oldName, String newName) {
        requireAdmin(admin);
        boolean renamed = categoryRepository.rename(oldName, newName);
        if (!renamed) {
            return false;
        }

        documentService.renameCategoryReferences(oldName, newName);

        for (User user : userRepository.findAll()) {
            Set<String> categories = user.getAllowedCategories();
            if (categories.remove(oldName)) {
                categories.add(newName);
                user.setAllowedCategories(categories);
                userRepository.update(user);
            }
        }

        return true;
    }

    public boolean deleteCategory(User admin, String category) {
        requireAdmin(admin);
        if (category == null || category.isBlank()) {
            return false;
        }

        boolean removed = categoryRepository.remove(category);
        if (!removed) {
            return false;
        }

        // Remove documents in the deleted category.
        documentService.listAll().stream()
                .filter(doc -> category.equals(doc.getCategory()))
                .forEach(doc -> documentService.deleteAndCleanupWatchState(doc.getId()));

        // Remove category from all users' allowed categories.
        for (User user : userRepository.findAll()) {
            Set<String> categories = user.getAllowedCategories();
            if (categories.remove(category)) {
                user.setAllowedCategories(categories);
                userRepository.update(user);
            }
        }

        return true;
    }

    private void requireAdmin(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        if (!user.canManageUsers()) {
            throw new IllegalStateException("User is not admin");
        }
    }

    private void validateUserInput(String firstName,
                                   String lastName,
                                   String username,
                                   String password,
                                   UserRole role,
                                   Set<String> allowedCategories) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        if (role != UserRole.ADMIN && (allowedCategories == null || allowedCategories.isEmpty())) {
            throw new IllegalArgumentException("At least one category is required");
        }
    }

    private User createUser(String firstName,
                            String lastName,
                            String username,
                            String password,
                            UserRole role,
                            Set<String> allowedCategories) {
        switch (role) {
            case ADMIN:
                return new Admin(firstName, lastName, username, password, allowedCategories);
            case AUTHOR:
                return new Author(firstName, lastName, username, password, allowedCategories);
            default:
                return new SimpleUser(firstName, lastName, username, password, allowedCategories);
        }
    }
}
