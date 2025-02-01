package com.hematify;
import com.google.firebase.firestore.PropertyName;


public class User {
    private String id; // For storing the document ID
    private String first_name; // Change from firstName to first_name
    private String last_name; // Change from lastName to last_name
    private String blood_group; // Change from bloodGroup to blood_group
    private String date_of_birth; // Change from dateOfBirth to date_of_birth
    private String email;
    private String gender;
    private String location;
    private String phone_no; // Change from phoneNo to phone_no
    private String profileImageBase64;

    // Default constructor is needed for Firestore
    public User() {}


    // Constructor
    public User(String id, String firstName, String lastName, String bloodGroup, String dateOfBirth,
                String email, String gender, String location, String phoneNo,String profileImageBase64) {
        this.id = id;
        first_name = firstName;
        last_name = lastName;
        blood_group = bloodGroup;
        date_of_birth = dateOfBirth;
        this.email = email;
        this.gender = gender;
        this.location = location;
        phone_no = phoneNo;
        this.profileImageBase64=profileImageBase64;
    }


    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getBlood_group() {
        return blood_group;
    }

    public void setBlood_group(String blood_group) {
        this.blood_group = blood_group;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }
}

