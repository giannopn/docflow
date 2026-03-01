package com.docflow.model;

import java.util.Set;

public class SimpleUser extends User {

    public SimpleUser(String firstName,
                      String lastName,
                      String username,
                      String password,
                      Set<String> allowedCategories) {
        super(firstName, lastName, username, password, allowedCategories);
    }

    public UserRole getRole() {
        return UserRole.SIMPLE_USER;
    }
}
