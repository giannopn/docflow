package com.docflow.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Base abstract class for all users of the system.
 */
public abstract class User {

    protected String firstName;
    protected String lastName;
    protected String username;
    protected String password;

    /**
     * Categories that the user has access to.
     */
    protected Set<String> allowedCategories;

    /**
     * Document IDs that the user is following.
     */
    protected Set<String> followedDocuments;

    protected User(String firstName,
                   String lastName,
                   String username,
                   String password,
                   Set<String> allowedCategories) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.allowedCategories = new HashSet<>(allowedCategories);
        this.followedDocuments = new HashSet<>();
    }

    /* =======================
       Basic getters
       ======================= */

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getAllowedCategories() {
        return new HashSet<>(allowedCategories);
    }

    public Set<String> getFollowedDocuments() {
        return new HashSet<>(followedDocuments);
    }

    /* =======================
       Authentication
       ======================= */

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    /* =======================
       Categories
       ======================= */

    public boolean hasAccessToCategory(String category) {
        return allowedCategories.contains(category);
    }

    /* =======================
       Document following
       ======================= */

    public void followDocument(String documentId) {
        followedDocuments.add(documentId);
    }

    public void unfollowDocument(String documentId) {
        followedDocuments.remove(documentId);
    }

    public boolean isFollowing(String documentId) {
        return followedDocuments.contains(documentId);
    }

    /* =======================
       Utility
       ======================= */

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + getFullName() + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
