package com.healthdesk.model;

/**
 * Model class representing a Patient Bill.
 * Contains automated calculation methods.
 */
public class Bill {
    private int id;
    private int patientId;
    private String patientName;
    private double consultationFee;
    private double labFee;
    private double bedFee;
    private double medicineFee;
    private double otherFee;
    private double total;
    private String date;
    private String status; // Pending, Paid

    public Bill() {
        this.status = "Pending";
    }

    public Bill(int id, int patientId, double consultationFee, double labFee, double bedFee, 
                double medicineFee, double otherFee, double total, String date, String status) {
        this.id = id;
        this.patientId = patientId;
        this.consultationFee = consultationFee;
        this.labFee = labFee;
        this.bedFee = bedFee;
        this.medicineFee = medicineFee;
        this.otherFee = otherFee;
        this.total = total > 0 ? total : calculateTotal();
        this.date = date;
        this.status = (status != null && !status.isEmpty()) ? status : "Pending";
    }

    public Bill(int patientId, double consultationFee, double labFee, double bedFee, 
                double medicineFee, double otherFee, String date) {
        this(0, patientId, consultationFee, labFee, bedFee, medicineFee, otherFee, 0.0, date, "Pending");
        this.total = calculateTotal();
    }

    /**
     * Automatically calculates the total charges for the bill.
     */
    public double calculateTotal() {
        this.total = consultationFee + labFee + bedFee + medicineFee + otherFee;
        return this.total;
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

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        calculateTotal();
    }

    public double getLabFee() {
        return labFee;
    }

    public void setLabFee(double labFee) {
        this.labFee = labFee;
        calculateTotal();
    }

    public double getBedFee() {
        return bedFee;
    }

    public void setBedFee(double bedFee) {
        this.bedFee = bedFee;
        calculateTotal();
    }

    public double getMedicineFee() {
        return medicineFee;
    }

    public void setMedicineFee(double medicineFee) {
        this.medicineFee = medicineFee;
        calculateTotal();
    }

    public double getOtherFee() {
        return otherFee;
    }

    public void setOtherFee(double otherFee) {
        this.otherFee = otherFee;
        calculateTotal();
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Bill #" + id + " for " + getPatientName() + ": Total $" + String.format("%.2f", total) + " (" + status + ")";
    }
}
