package com.sithum.pizzaapp.models;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String branch; // Added branch field
    private String profileImageUrl;
    private Long createdAt;
    private Long updatedAt;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String id, String fullName, String email, String phone, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getBranch() {
        return branch;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return fullName;
    }
}