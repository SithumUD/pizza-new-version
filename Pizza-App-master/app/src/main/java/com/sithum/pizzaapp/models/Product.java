package com.sithum.pizzaapp.models;

import java.util.List;
import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String status;
    private int imageResource; // For local drawable resources
    private String imageUrl; // For Cloudinary URLs
    private List<CustomizationOption> customizationOptions;
    private long createdAt;
    private long updatedAt;

    // Default constructor (required for Firebase)
    public Product() {}

    // Constructor with drawable resource
    public Product(String id, String name, String description, double price,
                   String category, String status, int imageResource,
                   List<CustomizationOption> customizationOptions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.imageResource = imageResource;
        this.customizationOptions = customizationOptions;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor with image URL
    public Product(String id, String name, String description, double price,
                   String category, String status, String imageUrl,
                   List<CustomizationOption> customizationOptions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.imageUrl = imageUrl;
        this.customizationOptions = customizationOptions;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<CustomizationOption> getCustomizationOptions() {
        return customizationOptions;
    }

    public void setCustomizationOptions(List<CustomizationOption> customizationOptions) {
        this.customizationOptions = customizationOptions;
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

    // Utility method to check if product has online image
    public boolean hasOnlineImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    // Utility method to get display image (prioritizes online image)
    public String getDisplayImageUrl() {
        return hasOnlineImage() ? imageUrl : null;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", customizationOptions=" + (customizationOptions != null ? customizationOptions.size() : 0) +
                '}';
    }
}