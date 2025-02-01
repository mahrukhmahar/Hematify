package com.hematify.fragments;

import com.google.firebase.Timestamp;

public class BloodRequest {
    private Timestamp deadline; // Change deadline to Timestamp
    private String patientName;
    private String location;
    private String bloodType;
    private String userImage;
    private String phone_no;
    private String reason;
    private Timestamp timestamp;// URL or local resource ID

    // Empty constructor for Firestore to use
    public BloodRequest() {
        // Firestore requires an empty constructor
    }


    // Getters and Setters
    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getDeadline() {
        return deadline; // Now returns Timestamp
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline; // Set as Timestamp
    }


    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
}
