package com.healthdesk.model;

/**
 * Model class representing a Prescription.
 */
public class Prescription {
    private int id;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String medicine;
    private String dosage;
    private String duration;
    private String instructions;
    private String date;

    public Prescription() {}

    public Prescription(int id, int patientId, int doctorId, String medicine, 
                        String dosage, String duration, String instructions, String date) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.medicine = medicine;
        this.dosage = dosage;
        this.duration = duration;
        this.instructions = instructions;
        this.date = date;
    }

    public Prescription(int patientId, int doctorId, String medicine, 
                        String dosage, String duration, String instructions, String date) {
        this(0, patientId, doctorId, medicine, dosage, duration, instructions, date);
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

    public String getPatientName() {
        return patientName != null ? patientName : "Patient #" + patientId;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName != null ? doctorName : "Doctor #" + doctorId;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getMedicine() {
        return medicine;
    }

    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return medicine + " (" + dosage + ", " + duration + ") - " + instructions;
    }
}
