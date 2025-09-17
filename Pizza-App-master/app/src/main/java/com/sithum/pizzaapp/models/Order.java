package com.sithum.pizzaapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

public class Order implements Parcelable {
    private String orderId;
    private String userId;
    private String branchId;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private String deliveryAddress;
    private double subtotal;
    private double deliveryFee;
    private double total;
    private long createdAt;
    private List<Map<String, Object>> items;

    // Customer info (will be loaded separately)
    private String customerName;
    private String customerPhone;

    public Order() {}

    protected Order(Parcel in) {
        orderId = in.readString();
        userId = in.readString();
        branchId = in.readString();
        status = in.readString();
        paymentMethod = in.readString();
        paymentStatus = in.readString();
        deliveryAddress = in.readString();
        subtotal = in.readDouble();
        deliveryFee = in.readDouble();
        total = in.readDouble();
        createdAt = in.readLong();
        customerName = in.readString();
        customerPhone = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeString(userId);
        dest.writeString(branchId);
        dest.writeString(status);
        dest.writeString(paymentMethod);
        dest.writeString(paymentStatus);
        dest.writeString(deliveryAddress);
        dest.writeDouble(subtotal);
        dest.writeDouble(deliveryFee);
        dest.writeDouble(total);
        dest.writeLong(createdAt);
        dest.writeString(customerName);
        dest.writeString(customerPhone);
    }
}