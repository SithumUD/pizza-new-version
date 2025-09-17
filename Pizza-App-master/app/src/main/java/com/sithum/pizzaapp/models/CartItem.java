package com.sithum.pizzaapp.models;

import java.util.List;

public class CartItem {
    private String id; // Firestore document ID
    private String productId;
    private String productName;
    private double basePrice;
    private int quantity;
    private List<CustomizationOption> selectedOptions;
    private double totalPrice;
    private String branchId;
    private String userId;
    private long timestamp;

    // Constructors
    public CartItem() {}

    public CartItem(String productId, String productName, double basePrice, int quantity,
                    List<CustomizationOption> selectedOptions, double totalPrice,
                    String branchId, String userId, long timestamp) {
        this.productId = productId;
        this.productName = productName;
        this.basePrice = basePrice;
        this.quantity = quantity;
        this.selectedOptions = selectedOptions;
        this.totalPrice = totalPrice;
        this.branchId = branchId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public List<CustomizationOption> getSelectedOptions() { return selectedOptions; }
    public void setSelectedOptions(List<CustomizationOption> selectedOptions) { this.selectedOptions = selectedOptions; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getOptionsString() {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return "";
        }
        StringBuilder options = new StringBuilder();
        for (CustomizationOption option : selectedOptions) {
            options.append(option.getName()).append(" +LKR ").append((int)option.getPrice()).append(", ");
        }
        return options.length() > 0 ? options.substring(0, options.length() - 2) : "";
    }
}