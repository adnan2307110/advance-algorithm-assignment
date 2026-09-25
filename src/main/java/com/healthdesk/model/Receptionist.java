package com.healthdesk.model;

public class Receptionist extends User {
    public Receptionist(int id, String username, String password, String fullName) {
        super(id, username, password, "Receptionist", fullName);
    }

    @Override
    public String getRoleDisplayName() {
        return "Front Desk Receptionist";
    }
}
