package com.sithum.pizzaapp.models;

public class PaymentMethod {
    private String id;
    private String cardNumber;
    private String cardholderName;
    private String expiryDate;
    private String cvv;
    private String cardType; // "visa", "mastercard", "amex"
    private boolean isDefault;
    private long timestamp;

    public PaymentMethod() {
        // Required empty constructor for Firebase
    }

    public PaymentMethod(String cardNumber, String cardholderName, String expiryDate,
                         String cvv, String cardType, boolean isDefault) {
        this.cardNumber = cardNumber;
        this.cardholderName = cardholderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardType = cardType;
        this.isDefault = isDefault;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Helper method to get masked card number
    public String getMaskedCardNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            String lastFour = cardNumber.substring(cardNumber.length() - 4);
            return "•••• •••• •••• " + lastFour;
        }
        return "•••• •••• •••• ••••";
    }

    // Helper method to detect card type from number
    public static String detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 2) {
            return "unknown";
        }

        cardNumber = cardNumber.replaceAll("\\s", "");

        if (cardNumber.startsWith("4")) {
            return "visa";
        } else if (cardNumber.matches("^5[1-5].*") || cardNumber.matches("^2[2-7].*")) {
            return "mastercard";
        } else if (cardNumber.matches("^3[47].*")) {
            return "amex";
        }

        return "unknown";
    }
}