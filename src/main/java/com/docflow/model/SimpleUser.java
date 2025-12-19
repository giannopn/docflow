package com.docflow.model;

import java.util.Set;

/**
 * A basic user who can only read documents in allowed categories
 * and manage document subscriptions (follow/unfollow).
 */
public class SimpleUser extends User {

    public SimpleUser(String firstName,
                      String lastName,
                      String username,
                      String password,
                      Set<String> allowedCategories) {
        super(firstName, lastName, username, password, allowedCategories);
    }

    /**
     * Returns the role name for simple UI/persistence logic.
     */
    public String getRole() {
        return "SIMPLE_USER";
    }
}
