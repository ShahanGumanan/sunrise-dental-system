package com.sunrisedental.model;

import java.time.LocalDate;

public class Patient {
    private int id;
    private String name;
    private String address;
    private String contactNumber;
    private LocalDate dateOfBirth;
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    @Override
    public String toString() {
        return name + " (" + contactNumber + ")"; 
    }
}