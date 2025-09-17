package com.sithum.pizzaapp.models;

public class Branch {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String manager;
    private String status;
    private long createdAt;
    private long updatedAt;

    // Constructors
    public Branch() {
        // Default constructor required for Firestore
    }

    public Branch(String name, String address, String phone, String manager) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.manager = manager;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}