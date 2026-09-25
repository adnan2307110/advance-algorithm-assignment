package com.healthdesk.model;

public class Admin extends User {
    public Admin(int id, String username, String password, String fullName) {
        super(id, username, password, "Admin", fullName);
    }

    @Override
    public String getRoleDisplayName() {
        return "System Administrator";
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}
