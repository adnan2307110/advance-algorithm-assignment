package com.healthdesk.model;

/**
 * Model class representing a laboratory test.
 */
public class LabTest {
    private int id;
    private int patientId;
    private String patientName;
    private int doctorId;
    private String doctorName;
    private String testName;
    private String result;
    private String status; // Requested, Processing, Completed
    private String date;

    public LabTest() {}

    public LabTest(int id, int patientId, int doctorId, String testName, String result, String status, String date) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testName = testName;
        this.result = result;
        this.status = status;
        this.date = date;
    }

    public LabTest(int patientId, int doctorId, String testName, String result, String status, String date) {
        this(0, patientId, doctorId, testName, result, status, date);
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

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Lab Test #" + id + ": " + testName + " for " + getPatientName() + " (" + status + ")";
    }
}
