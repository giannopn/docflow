package com.docflow.model;

import java.util.Set;

public class Author extends SimpleUser {

    public Author(String firstName,
                  String lastName,
                  String username,
                  String password,
                  Set<String> allowedCategories) {
        super(firstName, lastName, username, password, allowedCategories);
    }

    @Override
    public UserRole getRole() {
        return UserRole.AUTHOR;
    }
}
