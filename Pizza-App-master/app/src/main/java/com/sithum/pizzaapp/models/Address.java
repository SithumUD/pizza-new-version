package com.sithum.pizzaapp.models;

public class Address {
    private String id;
    private String name;
    private String fullAddress;
    private boolean isDefault;
    private String userId;
    private long timestamp;

    public Address() {
        // Default constructor required for Firestore
    }

    public Address(String name, String fullAddress, boolean isDefault, String userId) {
        this.name = name;
        this.fullAddress = fullAddress;
        this.isDefault = isDefault;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
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

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                ", isDefault=" + isDefault +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}