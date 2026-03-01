package com.docflow.model;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class User {

    protected String firstName;
    protected String lastName;
    protected String username;
    protected String password;

    protected Set<String> allowedCategories;

    protected Set<String> followedDocuments;

    protected Map<String, Integer> lastSeenVersions;

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
        this.lastSeenVersions = new HashMap<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getAllowedCategories() {
        return new HashSet<>(allowedCategories);
    }

    public Set<String> getFollowedDocuments() {
        return new HashSet<>(followedDocuments);
    }

    public Map<String, Integer> getLastSeenVersions() {
        return new HashMap<>(lastSeenVersions);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAllowedCategories(Set<String> categories) {
        this.allowedCategories = new HashSet<>(categories);
    }

    public void setFollowedDocuments(Set<String> followedDocuments) {
        this.followedDocuments = new HashSet<>(followedDocuments);
    }

    public void setLastSeenVersions(Map<String, Integer> lastSeenVersions) {
        this.lastSeenVersions = new HashMap<>(lastSeenVersions);
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public boolean hasAccessToCategory(String category) {
        if (getRole() == UserRole.ADMIN) {
            return true;
        }
        return allowedCategories.contains(category);
    }

    public void followDocument(String documentId) {
        followedDocuments.add(documentId);
        lastSeenVersions.putIfAbsent(documentId, 0);
    }

    public void unfollowDocument(String documentId) {
        followedDocuments.remove(documentId);
        lastSeenVersions.remove(documentId);
    }

    public boolean isFollowing(String documentId) {
        return followedDocuments.contains(documentId);
    }

    public int getLastSeenVersion(String documentId) {
        return lastSeenVersions.getOrDefault(documentId, 0);
    }

    public void markDocumentVersionSeen(String documentId, int versionNumber) {
        if (!followedDocuments.contains(documentId)) {
            return;
        }
        lastSeenVersions.put(documentId, versionNumber);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public abstract UserRole getRole();

    public boolean canManageDocuments() {
        return getRole() == UserRole.AUTHOR || getRole() == UserRole.ADMIN;
    }

    public boolean canManageUsers() {
        return getRole() == UserRole.ADMIN;
    }

    public boolean canManageCategories() {
        return getRole() == UserRole.ADMIN;
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
