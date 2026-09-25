package com.healthdesk.model;

/**
 * Model class representing an Appointment.
 */
public class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private String date; // YYYY-MM-DD
    private String time; // HH:mm
    private String status; // Scheduled, Completed, Cancelled

    // UI display helpers
    private String patientName;
    private String doctorName;
    private String doctorSpecialization;

    public Appointment() {}

    public Appointment(int id, int patientId, int doctorId, String date, String time, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public Appointment(int patientId, int doctorId, String date, String time, String status) {
        this(0, patientId, doctorId, date, time, status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPatientName() {
        return patientName != null ? patientName : "Patient #" + patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName != null ? doctorName : "Doctor #" + doctorId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization != null ? doctorSpecialization : "";
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    @Override
    public String toString() {
        return "Appointment #" + id + ": " + getPatientName() + " with " + getDoctorName() + " on " + date + " " + time;
    }
}
