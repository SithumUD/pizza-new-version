package com.sithum.pizzaapp.models;

import com.google.firebase.Timestamp;

public class SupportTicket {
    private String id;
    private String subject;
    private String message;
    private String orderNumber;
    private Timestamp timestamp;
    private String status;
    private String device;
    private String appVersion;

    // Default constructor (required for Firestore)
    public SupportTicket() {}

    public SupportTicket(String id, String subject, String message, String orderNumber,
                         Timestamp timestamp, String status, String device, String appVersion) {
        this.id = id;
        this.subject = subject;
        this.message = message;
        this.orderNumber = orderNumber;
        this.timestamp = timestamp;
        this.status = status;
        this.device = device;
        this.appVersion = appVersion;
    }

    // Getters
    public String getId() { return id; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public String getOrderNumber() { return orderNumber; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getDevice() { return device; }
    public String getAppVersion() { return appVersion; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setMessage(String message) { this.message = message; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setDevice(String device) { this.device = device; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    // Helper methods
    public boolean hasOrderNumber() {
        return orderNumber != null && !orderNumber.trim().isEmpty();
    }

    public String getFormattedTimestamp() {
        if (timestamp == null) return "Unknown";

        long now = System.currentTimeMillis();
        long ticketTime = timestamp.toDate().getTime();
        long diff = now - ticketTime;

        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);

        if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }
}