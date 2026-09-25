package com.healthdesk.model;

/**
 * Model class representing a Patient.
 */
public class Patient {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String address;
    private String emergencyContact;

    public Patient() {}

    public Patient(int id, String name, int age, String gender, String bloodGroup, 
                   String phone, String address, String emergencyContact) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.address = address;
        this.emergencyContact = emergencyContact;
    }

    public Patient(String name, int age, String gender, String bloodGroup, 
                   String phone, String address, String emergencyContact) {
        this(0, name, age, gender, bloodGroup, phone, address, emergencyContact);
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String toString() {
        return name + " (ID: " + id + ", " + bloodGroup + ")";
    }
}
