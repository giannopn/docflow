package com.docflow.model;

import java.util.Set;

public class Admin extends Author {

    public Admin(String firstName,
                 String lastName,
                 String username,
                 String password,
                 Set<String> allowedCategories) {
        super(firstName, lastName, username, password, allowedCategories);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
