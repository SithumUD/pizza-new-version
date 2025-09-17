package com.sithum.pizzaapp.models;

public class CustomizationOption {
    private String name;
    private double price;
    private boolean selected;

    public CustomizationOption() {}

    public CustomizationOption(String name, double price) {
        this.name = name;
        this.price = price;
        this.selected = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}