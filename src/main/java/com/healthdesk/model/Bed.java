package com.healthdesk.model;

/**
 * Model class representing a hospital bed.
 */
public class Bed {
    private String id; // e.g. B-101
    private String ward; // General, ICU, Surgery, Maternity
    private String status; // Available, Occupied, Maintenance
    private Integer patientId; // nullable if available
    private String patientName;

    public Bed() {}

    public Bed(String id, String ward, String status, Integer patientId) {
        this.id = id;
        this.ward = ward;
        this.status = status;
        this.patientId = patientId;
    }

    public Bed(String id, String ward, String status) {
        this(id, ward, status, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName != null ? patientName : (patientId != null ? "Patient #" + patientId : "None");
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return "Occupied".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return id + " (" + ward + " - " + status + ")";
    }
}
