package com.healthdesk.model;

public class LabTechnician extends User {
    public LabTechnician(int id, String username, String password, String fullName) {
        super(id, username, password, "Lab Technician", fullName);
    }

    @Override
    public String getRoleDisplayName() {
        return "Laboratory Medical Technician";
    }
}
