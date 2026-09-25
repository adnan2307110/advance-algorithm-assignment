package com.healthdesk.model;

/**
 * Base User class representing system credentials and roles.
 * Demonstrates OOP Encapsulation and Polymorphism.
 */
public abstract class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String fullName;

    public User(int id, String username, String password, String role, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Polymorphic method implemented by role-specific subclasses.
     */
    public abstract String getRoleDisplayName();

    /**
     * Polymorphic check whether user has administrative privileges.
     */
    public boolean isAdmin() {
        return false;
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
