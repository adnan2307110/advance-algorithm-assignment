package com.healthdesk.model;

/**
 * Represents an emergency patient in the hospital triage queue.
 * Implements Comparable for Java PriorityQueue sorting:
 * Priority 1 (Emergency) > Priority 2 (Critical) > Priority 3 (Normal).
 * Ties are broken by arrival time (FIFO).
 */
public class EmergencyPatient implements Comparable<EmergencyPatient> {
    private int id;
    private int patientId;
    private String patientName;
    private int priority; // 1 = Emergency, 2 = Critical, 3 = Normal
    private String condition;
    private String status; // Waiting, In Treatment, Discharged
    private long arrivalTime; // Milliseconds timestamp for FIFO tie-breaker

    public EmergencyPatient() {
        this.arrivalTime = System.currentTimeMillis();
    }

    public EmergencyPatient(int id, int patientId, String patientName, int priority, String condition, String status, long arrivalTime) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.priority = priority;
        this.condition = condition;
        this.status = status;
        this.arrivalTime = arrivalTime > 0 ? arrivalTime : System.currentTimeMillis();
    }

    public EmergencyPatient(int patientId, String patientName, int priority, String condition, String status) {
        this(0, patientId, patientName, priority, condition, status, System.currentTimeMillis());
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getPriorityLabel() {
        switch (priority) {
            case 1: return "1 - Emergency";
            case 2: return "2 - Critical";
            case 3: return "3 - Normal";
            default: return String.valueOf(priority);
        }
    }

    @Override
    public int compareTo(EmergencyPatient other) {
        if (this.priority != other.priority) {
            return Integer.compare(this.priority, other.priority);
        }
        return Long.compare(this.arrivalTime, other.arrivalTime);
    }

    @Override
    public String toString() {
        return "Priority " + priority + " | " + getPatientName() + " (" + condition + ") - " + status;
    }
}
