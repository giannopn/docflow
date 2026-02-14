package com.docflow.service;

import com.docflow.model.User;
import com.docflow.repository.UserRepository;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

    public void logout() {
        currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
