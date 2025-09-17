package com.sithum.pizzaapp.activities.utils;

import com.sithum.pizzaapp.models.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(CartItem item) {
        // Check if similar item exists (same product, same options)
        CartItem existingItem = findSimilarItem(item);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            existingItem.setTotalPrice(existingItem.getTotalPrice() + item.getTotalPrice());
        } else {
            cartItems.add(item);
        }
    }

    private CartItem findSimilarItem(CartItem newItem) {
        for (CartItem existingItem : cartItems) {
            if (existingItem.getProductId().equals(newItem.getProductId()) &&
                    haveSameOptions(existingItem.getSelectedOptions(), newItem.getSelectedOptions())) {
                return existingItem;
            }
        }
        return null;
    }

    private boolean haveSameOptions(List<com.sithum.pizzaapp.models.CustomizationOption> options1,
                                    List<com.sithum.pizzaapp.models.CustomizationOption> options2) {
        if (options1.size() != options2.size()) return false;

        for (com.sithum.pizzaapp.models.CustomizationOption option1 : options1) {
            boolean found = false;
            for (com.sithum.pizzaapp.models.CustomizationOption option2 : options2) {
                if (option1.getName().equals(option2.getName()) &&
                        option1.getPrice() == option2.getPrice()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}