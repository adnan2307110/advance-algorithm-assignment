package com.healthdesk.model;

/**
 * Model class representing a Doctor.
 */
public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String phone;
    private String room;
    private String availableTime;

    public Doctor() {}

    public Doctor(int id, String name, String specialization, String phone, String room, String availableTime) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.room = room;
        this.availableTime = availableTime;
    }

    public Doctor(String name, String specialization, String phone, String room, String availableTime) {
        this(0, name, specialization, phone, room, availableTime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }

    @Override
    public String toString() {
        return name + " (" + specialization + ", Room " + room + ")";
    }
}
