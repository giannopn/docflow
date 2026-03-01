package com.docflow.service;

import com.docflow.model.User;
import com.docflow.repository.UserRepository;

import java.util.Optional;

/**
 * Handles authentication and in-memory session state for the active user.
 */
public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Attempts to authenticate a user with username and password.
     *
     * @param username the account username
     * @param password the account password
     * @return an {@link Optional} containing the authenticated user when credentials are valid;
     *         otherwise an empty {@link Optional}
     */
    public Optional<User> login(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return Optional.empty();
        }

        if (!user.get().checkPassword(password)) {
            return Optional.empty();
        }

        currentUser = user.get();
        return Optional.of(currentUser);
    }

    /**
     * Returns the current authenticated user, if any.
     *
     * @return an {@link Optional} containing the current user when logged in;
     *         otherwise an empty {@link Optional}
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
