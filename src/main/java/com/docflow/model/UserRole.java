package com.docflow.model;

public enum UserRole {
    SIMPLE_USER,
    AUTHOR,
    ADMIN;

    public static UserRole fromStorageValue(String value) {
        if (value == null || value.isBlank()) {
            return SIMPLE_USER;
        }

        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SIMPLE_USER;
        }
    }
}
