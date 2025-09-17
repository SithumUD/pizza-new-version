package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.CheckoutItemAdapter;
import com.sithum.pizzaapp.models.CartItem;
import com.sithum.pizzaapp.models.CustomizationOption;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";
    private static final String PREFS_NAME = "CartPrefs";
    private static final String KEY_CART_DATA = "cart_data";
    private static final String KEY_SUBTOTAL = "subtotal";
    private static final String KEY_DELIVERY_FEE = "delivery_fee";
    private static final String KEY_TOTAL = "total";
    private static final String KEY_BRANCH_ID = "branch_id";

    private TextView tvDeliveryAddress, tvTotal;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCashOnDelivery, rbCreditCard;
    private Button btnPlaceOrder;
    private RecyclerView itemRecyclerView;
    private ImageView btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private double subtotal = 0;
    private double deliveryFee = 200;
    private double total = 0;
    private String branchId;
    private List<CartItem> cartItems = new ArrayList<>();
    private String deliveryAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting CheckoutActivity");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase and SharedPreferences
        Log.d(TAG, "onCreate: Initializing Firebase and SharedPreferences");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        gson = new Gson();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "onCreate: User is authenticated - UID: " + currentUser.getUid());
        } else {
            Log.e(TAG, "onCreate: User is NOT authenticated!");
        }

        initViews();
        setupRecyclerView();
        loadCartDataFromLocalStorage();
        loadDefaultAddress();
        setupClickListeners();

        Log.d(TAG, "onCreate: CheckoutActivity initialization completed");
    }

    private void initViews() {
        Log.d(TAG, "initViews: Initializing UI views");

        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvTotal = findViewById(R.id.tvTotal);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        rbCashOnDelivery = findViewById(R.id.rbCashOnDelivery);
        rbCreditCard = findViewById(R.id.rbCreditCard);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        itemRecyclerView = findViewById(R.id.itemlist);
        btnBack = findViewById(R.id.btnBack);

        // Check if all views are found
        if (tvDeliveryAddress == null) Log.e(TAG, "initViews: tvDeliveryAddress is NULL!");
        if (tvTotal == null) Log.e(TAG, "initViews: tvTotal is NULL!");
        if (btnPlaceOrder == null) Log.e(TAG, "initViews: btnPlaceOrder is NULL!");
        if (itemRecyclerView == null) Log.e(TAG, "initViews: itemRecyclerView is NULL!");

        Log.d(TAG, "initViews: All views initialized successfully");
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Setting up RecyclerView");
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "setupRecyclerView: RecyclerView setup completed");
    }

    private void loadCartDataFromLocalStorage() {
        Log.d(TAG, "loadCartDataFromLocalStorage: Starting to load cart data");

        try {
            // Retrieve cart data from SharedPreferences
            String cartDataJson = sharedPreferences.getString(KEY_CART_DATA, "");
            Log.d(TAG, "loadCartDataFromLocalStorage: Cart data JSON length: " + cartDataJson.length());

            if (cartDataJson.isEmpty()) {
                Log.e(TAG, "loadCartDataFromLocalStorage: No cart data found in SharedPreferences");
                finish();
                return;
            }

            // Parse cart items from JSON
            Type cartItemListType = new TypeToken<List<CartItem>>(){}.getType();
            cartItems = gson.fromJson(cartDataJson, cartItemListType);

            if (cartItems == null) {
                cartItems = new ArrayList<>();
                Log.e(TAG, "loadCartDataFromLocalStorage: Cart items is NULL after parsing");
            }

            Log.d(TAG, "loadCartDataFromLocalStorage: Parsed " + cartItems.size() + " cart items");

            // Retrieve totals and branch ID
            subtotal = sharedPreferences.getFloat(KEY_SUBTOTAL, 0f);
            deliveryFee = sharedPreferences.getFloat(KEY_DELIVERY_FEE, 200f);
            total = sharedPreferences.getFloat(KEY_TOTAL, 0f);
            branchId = sharedPreferences.getString(KEY_BRANCH_ID, "");

            Log.d(TAG, "loadCartDataFromLocalStorage: Subtotal: " + subtotal);
            Log.d(TAG, "loadCartDataFromLocalStorage: Delivery Fee: " + deliveryFee);
            Log.d(TAG, "loadCartDataFromLocalStorage: Total: " + total);
            Log.d(TAG, "loadCartDataFromLocalStorage: Branch ID: " + branchId);

            // Display total
            tvTotal.setText(String.format("LKR %d", (int) total));

            // Setup recycler view with cart items
            setupCheckoutItemsRecyclerView();

            Toast.makeText(this, "Cart loaded: " + cartItems.size() + " items, Total: LKR " + (int)total, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "loadCartDataFromLocalStorage: Cart data loaded successfully");

        } catch (Exception e) {
            Log.e(TAG, "loadCartDataFromLocalStorage: ERROR loading cart data", e);
            finish();
        }
    }

    private void setupCheckoutItemsRecyclerView() {
        Log.d(TAG, "setupCheckoutItemsRecyclerView: Setting up checkout items");

        // Convert CartItem data to lists for the adapter
        List<String> productNames = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        for (CartItem item : cartItems) {
            productNames.add(item.getProductName());
            quantities.add(item.getQuantity());
            prices.add(item.getTotalPrice());
            Log.d(TAG, "setupCheckoutItemsRecyclerView: Item - " + item.getProductName() +
                    ", Qty: " + item.getQuantity() + ", Price: " + item.getTotalPrice());
        }

        CheckoutItemAdapter adapter = new CheckoutItemAdapter(productNames, quantities, prices);
        itemRecyclerView.setAdapter(adapter);

        Log.d(TAG, "setupCheckoutItemsRecyclerView: Adapter set with " + productNames.size() + " items");
        Toast.makeText(this, "Items displayed: " + productNames.size(), Toast.LENGTH_SHORT).show();
    }

    private void loadDefaultAddress() {
        Log.d(TAG, "loadDefaultAddress: Starting to load default address");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "loadDefaultAddress: Current user is NULL");
            Toast.makeText(this, "ERROR: Please login to continue", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "loadDefaultAddress: User UID: " + currentUser.getUid());

        // Show loading state
        tvDeliveryAddress.setText("Loading address...");

        // Query for default address
        db.collection("addresses")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "loadDefaultAddress: Query successful, documents found: " +
                            queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Found default address
                        DocumentSnapshot addressDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String addressName = addressDoc.getString("name");
                        String fullAddress = addressDoc.getString("fullAddress");

                        Log.d(TAG, "loadDefaultAddress: Found default address - Name: " +
                                addressName + ", Address: " + fullAddress);

                        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                            deliveryAddress = fullAddress;
                            // Display address with name if available
                            if (addressName != null && !addressName.trim().isEmpty()) {
                                tvDeliveryAddress.setText(addressName + "\n" + fullAddress);
                            } else {
                                tvDeliveryAddress.setText(fullAddress);
                            }

                            Log.d(TAG, "loadDefaultAddress: Default address set successfully");
                        } else {
                            // Handle case where address data is incomplete
                            Log.e(TAG, "loadDefaultAddress: Address data is incomplete");
                            tvDeliveryAddress.setText("Address data incomplete. Please update your address.");
                            deliveryAddress = "";
                        }
                    } else {
                        // No default address found, try to get any address
                        Log.w(TAG, "loadDefaultAddress: No default address found, trying any available");
                        loadAnyAvailableAddress(currentUser.getUid());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadDefaultAddress: Error loading default address", e);
                    tvDeliveryAddress.setText("Error loading address. Please try again.");
                    deliveryAddress = "";
                });
    }

    private void loadAnyAvailableAddress(String userId) {
        Log.d(TAG, "loadAnyAvailableAddress: Loading any available address for user: " + userId);

        // If no default address, try to get the first available address
        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "loadAnyAvailableAddress: Query successful, documents: " +
                            queryDocumentSnapshots.size());

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot addressDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String addressName = addressDoc.getString("name");
                        String fullAddress = addressDoc.getString("fullAddress");

                        Log.d(TAG, "loadAnyAvailableAddress: Found address - Name: " +
                                addressName + ", Address: " + fullAddress);

                        if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                            deliveryAddress = fullAddress;
                            String displayText = "No default address set.\n";
                            if (addressName != null && !addressName.trim().isEmpty()) {
                                displayText += addressName + "\n" + fullAddress;
                            } else {
                                displayText += fullAddress;
                            }
                            tvDeliveryAddress.setText(displayText);

                            Toast.makeText(this, "Using available address", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "loadAnyAvailableAddress: Available address set successfully");
                        } else {
                            Log.e(TAG, "loadAnyAvailableAddress: Available address data incomplete");
                            showNoAddressMessage();
                        }
                    } else {
                        Log.w(TAG, "loadAnyAvailableAddress: No addresses found for user");
                        showNoAddressMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadAnyAvailableAddress: Error loading available address", e);
                    showNoAddressMessage();
                });
    }

    private void showNoAddressMessage() {
        Log.w(TAG, "showNoAddressMessage: No addresses found");
        tvDeliveryAddress.setText("No addresses found. Please add an address to continue.");
        deliveryAddress = "";
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners");

        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "btnBack clicked: Finishing activity");
            finish();
        });

        findViewById(R.id.btnEditAddress).setOnClickListener(v -> {
            Log.d(TAG, "btnEditAddress clicked: Navigating to AddressActivity");
            // Navigate to AddressActivity
            Intent intent = new Intent(CheckoutActivity.this, AddressActivity.class);
            startActivity(intent);
        });

        btnPlaceOrder.setOnClickListener(v -> {
            Log.d(TAG, "btnPlaceOrder clicked: Starting order placement");
            placeOrder();
        });

        // Add click listeners for payment method containers
        findViewById(R.id.layoutCashOnDelivery).setOnClickListener(v -> {
            Log.d(TAG, "layoutCashOnDelivery clicked: Selecting cash on delivery");
            rbCashOnDelivery.setChecked(true);
        });

        findViewById(R.id.layoutCreditCard).setOnClickListener(v -> {
            Log.d(TAG, "layoutCreditCard clicked: Selecting credit card");
            rbCreditCard.setChecked(true);
        });

        // Set default selection
        rbCashOnDelivery.setChecked(true);
        Log.d(TAG, "setupClickListeners: Default payment method set to Cash on Delivery");
        Log.d(TAG, "setupClickListeners: Click listeners setup completed");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed, reloading address");

        // Reload address when returning from AddressActivity
        loadDefaultAddress();
    }

    private void placeOrder() {
        Log.d(TAG, "placeOrder: Starting order placement process");


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "placeOrder: Current user is NULL");

            return;
        }

        Log.d(TAG, "placeOrder: User authenticated - UID: " + currentUser.getUid());

        if (cartItems.isEmpty()) {
            Log.e(TAG, "placeOrder: Cart is empty");
            return;
        }

        Log.d(TAG, "placeOrder: Cart has " + cartItems.size() + " items");

        if (deliveryAddress.isEmpty()) {
            Log.e(TAG, "placeOrder: Delivery address is empty");

            return;
        }

        Log.d(TAG, "placeOrder: Delivery address: " + deliveryAddress);

        // Get selected payment method
        String paymentMethod = "cash";
        String paymentStatus = "unpaid";

        if (rbCreditCard.isChecked()) {
            paymentMethod = "card";
            paymentStatus = "paid";
            Log.d(TAG, "placeOrder: Payment method: Credit Card");
        } else {
            Log.d(TAG, "placeOrder: Payment method: Cash on Delivery");
        }



        // Create order data
        String orderId = UUID.randomUUID().toString();
        Log.d(TAG, "placeOrder: Generated order ID: " + orderId);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", currentUser.getUid());
        orderData.put("branchId", branchId);
        orderData.put("subtotal", subtotal);
        orderData.put("deliveryFee", deliveryFee);
        orderData.put("total", total);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("paymentStatus", paymentStatus);
        orderData.put("status", "pending"); // pending, preparing, out_for_delivery, delivered, cancelled
        orderData.put("deliveryAddress", deliveryAddress);
        orderData.put("createdAt", System.currentTimeMillis());

        Log.d(TAG, "placeOrder: Order data created - Total: " + total + ", Branch: " + branchId);

        // Add order items from cart data
        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", cartItem.getProductId());
            item.put("productName", cartItem.getProductName());
            item.put("basePrice", cartItem.getBasePrice());
            item.put("quantity", cartItem.getQuantity());
            item.put("totalPrice", cartItem.getTotalPrice());

            // Convert customization options to string or keep as list
            List<String> optionNames = new ArrayList<>();
            if (cartItem.getSelectedOptions() != null) {
                for (CustomizationOption option : cartItem.getSelectedOptions()) {
                    optionNames.add(option.getName() + " (+LKR " + option.getPrice() + ")");
                }
            }
            item.put("options", optionNames);
            item.put("selectedOptions", cartItem.getSelectedOptions());

            orderItems.add(item);

            Log.d(TAG, "placeOrder: Added item - " + cartItem.getProductName() +
                    ", Qty: " + cartItem.getQuantity() + ", Price: " + cartItem.getTotalPrice());
        }
        orderData.put("items", orderItems);

        Log.d(TAG, "placeOrder: Order items added, total items: " + orderItems.size());

        // Show loading
        btnPlaceOrder.setText("Placing Order...");
        btnPlaceOrder.setEnabled(false);

        Log.d(TAG, "placeOrder: Button state changed to loading");


        // Save order to Firestore
        Log.d(TAG, "placeOrder: Starting Firestore save operation");

        db.collection("orders")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "placeOrder: SUCCESS - Order saved to Firestore: " + orderId);
                        Toast.makeText(CheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                        // Clear cart after successful order
                        Log.d(TAG, "placeOrder: Starting cart cleanup");
                        clearUserCart(currentUser.getUid());

                        // Clear cart data from local storage
                        clearCartDataFromLocalStorage();

                        // Navigate to order confirmation
                        Log.d(TAG, "placeOrder: Navigating to order tracking");
                        navigateToOrderConfirmation(orderId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "placeOrder: FAILED to save order to Firestore", e);
                        Toast.makeText(CheckoutActivity.this,
                                "FAILED to place order: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();

                        btnPlaceOrder.setText("Place Order");
                        btnPlaceOrder.setEnabled(true);
                        Log.d(TAG, "placeOrder: Button state reset after failure");
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "placeOrder: Firestore operation completed. Success: " + task.isSuccessful());
                        if (!task.isSuccessful() && task.getException() != null) {
                            Log.e(TAG, "placeOrder: Task completed with exception", task.getException());
                        }
                    }
                });

        Log.d(TAG, "placeOrder: Firestore save operation initiated");
    }

    private void clearUserCart(String userId) {
        Log.d(TAG, "clearUserCart: Starting to clear cart for user: " + userId);

        db.collection("cartItems")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "clearUserCart: Found " + queryDocumentSnapshots.size() +
                            " cart items to delete");

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("cartItems").document(document.getId()).delete();
                        Log.d(TAG, "clearUserCart: Deleting cart item: " + document.getId());
                    }

                    Log.d(TAG, "clearUserCart: Cart cleared from Firestore");

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "clearUserCart: Error clearing cart from Firestore", e);

                });
    }

    private void clearCartDataFromLocalStorage() {
        Log.d(TAG, "clearCartDataFromLocalStorage: Starting to clear local cart data");

        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_CART_DATA);
            editor.remove(KEY_SUBTOTAL);
            editor.remove(KEY_DELIVERY_FEE);
            editor.remove(KEY_TOTAL);
            editor.remove(KEY_BRANCH_ID);
            editor.apply();

            Log.d(TAG, "clearCartDataFromLocalStorage: Local cart data cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "clearCartDataFromLocalStorage: Error clearing local cart data", e);
        }
    }

    private void navigateToOrderConfirmation(String orderId) {
        Log.d(TAG, "navigateToOrderConfirmation: Navigating to OrderTrackingActivity with order ID: " + orderId);


        try {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("orderId", orderId);
            Log.d(TAG, "navigateToOrderConfirmation: Intent created with order ID");

            startActivity(intent);
            Log.d(TAG, "navigateToOrderConfirmation: Activity started");

            finish();
            Log.d(TAG, "navigateToOrderConfirmation: Current activity finished");

        } catch (Exception e) {
            Log.e(TAG, "navigateToOrderConfirmation: Error navigating to order tracking", e);

        }
    }
}