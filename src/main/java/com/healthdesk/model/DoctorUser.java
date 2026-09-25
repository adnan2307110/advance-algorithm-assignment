package com.healthdesk.model;

public class DoctorUser extends User {
    private int doctorId;

    public DoctorUser(int id, String username, String password, String fullName, int doctorId) {
        super(id, username, password, "Doctor", fullName);
        this.doctorId = doctorId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public String getRoleDisplayName() {
        return "Attending Physician / Doctor";
    }
}
