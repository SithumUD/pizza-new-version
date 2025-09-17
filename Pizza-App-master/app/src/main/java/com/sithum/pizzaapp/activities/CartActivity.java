package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.CartAdapter;
import com.sithum.pizzaapp.models.CartItem;
import com.sithum.pizzaapp.models.CustomizationOption;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private static final String TAG = "CartActivity";
    private static final String PREFS_NAME = "CartPrefs";
    private static final String KEY_CART_DATA = "cart_data";
    private static final String KEY_SUBTOTAL = "subtotal";
    private static final String KEY_DELIVERY_FEE = "delivery_fee";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_BRANCH_ID = "branch_id";

    private RecyclerView cartRecyclerView;
    private TextView txtSubtotal, txtDeliveryFee, txtTotal, txtcartempty;
    private Button btnCheckout;
    private ImageView btnBack;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase and SharedPreferences
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();

        initViews();
        setupRecyclerView();
        setupSwipeToDelete();
        setupClickListeners();
        loadCartItems();
    }

    private void initViews() {
        cartRecyclerView = findViewById(R.id.itemlist);
        txtSubtotal = findViewById(R.id.txtsubtotal);
        txtDeliveryFee = findViewById(R.id.txtdeliveryfee);
        txtTotal = findViewById(R.id.txttotal);
        txtcartempty = findViewById(R.id.txtcartempty);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems, this);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                CartItem removedItem = cartItems.get(position);
                removeCartItem(removedItem, position);
            }
        }).attachToRecyclerView(cartRecyclerView);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            saveCartDataToLocalStorage();
            navigateToCheckout();
        });
    }

    private void loadCartItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to view cart", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("cartItems")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CartItem cartItem = parseCartItem(document);
                        cartItem.setId(document.getId());
                        cartItems.add(cartItem);
                    }
                    cartAdapter.updateCartItems(cartItems);
                    updateBillSummary();

                    if (cartItems.isEmpty()) {
                        findViewById(R.id.scrollView).setVisibility(View.GONE);
                        findViewById(R.id.btnCheckout).setVisibility(View.GONE);
                        txtcartempty.setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.scrollView).setVisibility(View.VISIBLE);
                        findViewById(R.id.btnCheckout).setVisibility(View.VISIBLE);
                        txtcartempty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart items", e);
                    Toast.makeText(this, "Failed to load cart items", Toast.LENGTH_SHORT).show();
                });
    }

    private CartItem parseCartItem(DocumentSnapshot document) {
        CartItem cartItem = new CartItem();
        cartItem.setProductId(document.getString("productId"));
        cartItem.setProductName(document.getString("productName"));
        cartItem.setBasePrice(document.getDouble("basePrice"));
        cartItem.setQuantity(document.getLong("quantity").intValue());
        cartItem.setTotalPrice(document.getDouble("totalPrice"));
        cartItem.setBranchId(document.getString("branchId"));
        cartItem.setUserId(document.getString("userId"));
        cartItem.setTimestamp(document.getLong("timestamp"));

        // Parse selected options
        List<CustomizationOption> options = new ArrayList<>();
        List<Map<String, Object>> optionsData = (List<Map<String, Object>>) document.get("selectedOptions");
        if (optionsData != null) {
            for (Map<String, Object> optionMap : optionsData) {
                String name = (String) optionMap.get("name");
                Double price = (Double) optionMap.get("price");
                if (price == null) {
                    Long priceLong = (Long) optionMap.get("price");
                    price = priceLong != null ? priceLong.doubleValue() : 0.0;
                }
                options.add(new CustomizationOption(name, price));
            }
        }
        cartItem.setSelectedOptions(options);

        return cartItem;
    }

    private void updateBillSummary() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        double deliveryFee = 200; // Fixed delivery fee
        double total = subtotal + deliveryFee;

        txtSubtotal.setText(String.format("LKR %d", (int) subtotal));
        txtDeliveryFee.setText(String.format("LKR %d", (int) deliveryFee));
        txtTotal.setText(String.format("LKR %d", (int) total));
    }

    private void saveCartDataToLocalStorage() {
        try {
            // Convert cart items to JSON
            String cartDataJson = gson.toJson(cartItems);

            // Calculate totals
            double subtotal = calculateSubtotal();
            double deliveryFee = 200.0;
            double total = subtotal + deliveryFee;

            // Get branch ID (assuming all items from same branch)
            String branchId = !cartItems.isEmpty() ? cartItems.get(0).getBranchId() : "";

            // Save to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_CART_DATA, cartDataJson);
            editor.putFloat(KEY_SUBTOTAL, (float) subtotal);
            editor.putFloat(KEY_DELIVERY_FEE, (float) deliveryFee);
            editor.putFloat(KEY_TOTAL, (float) total);
            editor.putString(KEY_BRANCH_ID, branchId);
            editor.apply();

            Log.d(TAG, "Cart data saved to local storage");
        } catch (Exception e) {
            Log.e(TAG, "Error saving cart data to local storage", e);
        }
    }

    private void removeCartItem(CartItem cartItem, int position) {
        // Remove from Firestore
        db.collection("cartItems").document(cartItem.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    cartItems.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    updateBillSummary();

                    Snackbar.make(cartRecyclerView, "Item removed from cart", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> restoreCartItem(cartItem, position))
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing cart item", e);
                    cartAdapter.notifyItemChanged(position);
                });
    }

    private void restoreCartItem(CartItem cartItem, int position) {
        // Convert CartItem back to Map and restore to Firestore
        // For simplicity, we'll just reload all items
        loadCartItems();
    }

    private void navigateToCheckout() {
        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    // CartAdapter listener methods
    @Override
    public void onItemRemoved(CartItem cartItem, int position) {
        removeCartItem(cartItem, position);
    }

    @Override
    public void onQuantityIncreased(CartItem cartItem, int position) {
        updateItemQuantity(cartItem, position, cartItem.getQuantity() + 1);
    }

    @Override
    public void onQuantityDecreased(CartItem cartItem, int position) {
        if (cartItem.getQuantity() > 1) {
            updateItemQuantity(cartItem, position, cartItem.getQuantity() - 1);
        } else {
            removeCartItem(cartItem, position);
        }
    }

    private void updateItemQuantity(CartItem cartItem, int position, int newQuantity) {
        double newTotalPrice = (cartItem.getBasePrice() + getOptionsTotal(cartItem)) * newQuantity;

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", newQuantity);
        updates.put("totalPrice", newTotalPrice);
        updates.put("timestamp", System.currentTimeMillis());

        db.collection("cartItems").document(cartItem.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    cartItem.setQuantity(newQuantity);
                    cartItem.setTotalPrice(newTotalPrice);
                    cartAdapter.notifyItemChanged(position);
                    updateBillSummary();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating quantity", e);
                    cartAdapter.notifyItemChanged(position);
                });
    }

    private double getOptionsTotal(CartItem cartItem) {
        double optionsTotal = 0;
        if (cartItem.getSelectedOptions() != null) {
            for (CustomizationOption option : cartItem.getSelectedOptions()) {
                optionsTotal += option.getPrice();
            }
        }
        return optionsTotal;
    }

    // Static method to clear cart data from local storage (can be called from other activities)
    public static void clearCartDataFromLocalStorage(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CART_DATA);
        editor.remove(KEY_SUBTOTAL);
        editor.remove(KEY_DELIVERY_FEE);
        editor.remove(KEY_TOTAL);
        editor.remove(KEY_BRANCH_ID);
        editor.apply();
        Log.d(TAG, "Cart data cleared from local storage");
    }
}