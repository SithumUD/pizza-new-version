package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.CustomizationAdapter;
import com.sithum.pizzaapp.models.Product;
import com.sithum.pizzaapp.models.CustomizationOption;
import com.sithum.pizzaapp.models.CartItem;
import com.sithum.pizzaapp.activities.utils.CartManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodDetailsActivity extends AppCompatActivity {

    private static final String TAG = "FoodDetailsActivity";

    private String branchId;
    private String productId;
    private Product currentProduct;

    private ImageView imgfood, btnback;
    private TextView name, description, price, quantity;
    private RecyclerView optionRecycleview;
    private ImageButton btnminus, btnplus;
    private Button btnAddCart;

    private CustomizationAdapter customizationAdapter;
    private int currentQuantity = 1;
    private double basePrice = 0.0;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get data from intent
        Intent intent = getIntent();
        branchId = intent.getStringExtra("branch");
        productId = intent.getStringExtra("product");

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadProductFromFirestore();
        setupClickListeners();
    }

    private void initViews() {
        imgfood = findViewById(R.id.ivFoodImage);
        btnback = findViewById(R.id.ivBack);
        name = findViewById(R.id.tvFoodName);
        description = findViewById(R.id.tvFoodDescription);
        price = findViewById(R.id.tvFoodPrice);
        optionRecycleview = findViewById(R.id.optionRecyclerView);
        quantity = findViewById(R.id.tvQuantity);
        btnminus = findViewById(R.id.btnMinus);
        btnplus = findViewById(R.id.btnPlus);
        btnAddCart = findViewById(R.id.btnAddToCart);
    }

    private void setupRecyclerView() {
        optionRecycleview.setLayoutManager(new LinearLayoutManager(this));
        customizationAdapter = new CustomizationAdapter(new ArrayList<>(), this::updateTotalPrice);
        optionRecycleview.setAdapter(customizationAdapter);
    }

    private void loadProductFromFirestore() {
        // Show loading state
        btnAddCart.setText("Loading...");
        btnAddCart.setEnabled(false);

        db.collection("products")
                .document(productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            parseProductFromDocument(document);
                        } else {
                            Log.d(TAG, "No such document");
                            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.d(TAG, "Get failed with ", task.getException());
                        Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void parseProductFromDocument(DocumentSnapshot document) {
        try {
            String productName = document.getString("name");
            String productDescription = document.getString("description");
            String productCategory = document.getString("category");
            String productStatus = document.getString("status");
            String imageUrl = document.getString("imageUrl");
            Long createdAtLong = document.getLong("createdAt");

            // Handle price - could be Double or Long
            Double productPrice = document.getDouble("price");
            if (productPrice == null) {
                Long priceLong = document.getLong("price");
                productPrice = priceLong != null ? priceLong.doubleValue() : 0.0;
            }

            long createdAt = createdAtLong != null ? createdAtLong : System.currentTimeMillis();

            // Parse customization options
            List<CustomizationOption> customizationOptions = new ArrayList<>();
            List<Map<String, Object>> optionsData = (List<Map<String, Object>>) document.get("customizationOptions");

            if (optionsData != null) {
                for (Map<String, Object> optionMap : optionsData) {
                    String optionName = (String) optionMap.get("name");
                    Double optionPrice = (Double) optionMap.get("price");

                    // Handle if price is stored as Long
                    if (optionPrice == null) {
                        Long priceLong = (Long) optionMap.get("price");
                        optionPrice = priceLong != null ? priceLong.doubleValue() : 0.0;
                    }

                    if (optionName != null) {
                        customizationOptions.add(new CustomizationOption(optionName, optionPrice));
                    }
                }
            }

            // Create Product object
            currentProduct = new Product();
            currentProduct.setId(document.getId());
            currentProduct.setName(productName);
            currentProduct.setDescription(productDescription);
            currentProduct.setPrice(productPrice);
            currentProduct.setCategory(productCategory);
            currentProduct.setStatus(productStatus);
            currentProduct.setImageUrl(imageUrl);
            currentProduct.setCustomizationOptions(customizationOptions);
            currentProduct.setCreatedAt(createdAt);
            currentProduct.setUpdatedAt(createdAt);

            // Display the product
            displayProductData();

        } catch (Exception e) {
            Log.e(TAG, "Error parsing product data", e);
            Toast.makeText(this, "Error loading product data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductData() {
        if (currentProduct == null) return;

        name.setText(currentProduct.getName() != null ? currentProduct.getName() : "Unknown Product");
        description.setText(currentProduct.getDescription() != null ? currentProduct.getDescription() : "No description available");
        basePrice = currentProduct.getPrice();

        // Set image - you can use Glide or Picasso for online images
        if (currentProduct.hasOnlineImage()) {
            // Load from URL using Glide/Picasso
            // Glide.with(this).load(currentProduct.getImageUrl()).into(imgfood);
            // For now, using default image
            imgfood.setImageResource(R.drawable.pizza);
        } else {
            imgfood.setImageResource(R.drawable.pizza);
        }

        updatePriceDisplay();

        // Set customization options
        if (currentProduct.getCustomizationOptions() != null && !currentProduct.getCustomizationOptions().isEmpty()) {
            customizationAdapter.updateOptions(currentProduct.getCustomizationOptions());
        }

        // Enable add to cart button
        btnAddCart.setEnabled(true);
        updateAddToCartButton();
    }

    private void setupClickListeners() {
        // Back button
        btnback.setOnClickListener(v -> finish());

        // Quantity controls
        btnminus.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                quantity.setText(String.valueOf(currentQuantity));
                updateAddToCartButton();
            }
        });

        btnplus.setOnClickListener(v -> {
            currentQuantity++;
            quantity.setText(String.valueOf(currentQuantity));
            updateAddToCartButton();
        });

        // Add to cart button
        btnAddCart.setOnClickListener(v -> addToCart());
    }

    private void updateTotalPrice() {
        updateAddToCartButton();
    }

    private void updatePriceDisplay() {
        price.setText(String.format("LKR %.0f", basePrice));
    }

    private void updateAddToCartButton() {
        double totalPrice = calculateTotalPrice();
        btnAddCart.setText(String.format("Add to Cart - LKR %.0f", totalPrice * currentQuantity));
    }

    private double calculateTotalPrice() {
        double total = basePrice;
        if (customizationAdapter != null) {
            for (CustomizationOption option : customizationAdapter.getSelectedOptions()) {
                total += option.getPrice();
            }
        }
        return total;
    }

    private void addToCart() {
        if (currentProduct == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create cart item
            CartItem cartItem = new CartItem();
            cartItem.setProductId(currentProduct.getId());
            cartItem.setProductName(currentProduct.getName());
            cartItem.setBasePrice(basePrice);
            cartItem.setQuantity(currentQuantity);
            cartItem.setSelectedOptions(new ArrayList<>(customizationAdapter.getSelectedOptions()));
            cartItem.setTotalPrice(calculateTotalPrice() * currentQuantity);
            cartItem.setBranchId(branchId);
            cartItem.setTimestamp(System.currentTimeMillis());

            // Add to local cart manager
            CartManager.getInstance().addItem(cartItem);

            // Store in Firestore
            storeCartItemInFirestore(cartItem, currentUser.getUid());

        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            Toast.makeText(this, "Error adding to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeCartItemInFirestore(CartItem cartItem, String userId) {
        // Create a map for the cart item
        Map<String, Object> cartItemMap = new HashMap<>();
        cartItemMap.put("productId", cartItem.getProductId());
        cartItemMap.put("productName", cartItem.getProductName());
        cartItemMap.put("basePrice", cartItem.getBasePrice());
        cartItemMap.put("quantity", cartItem.getQuantity());
        cartItemMap.put("totalPrice", cartItem.getTotalPrice());
        cartItemMap.put("branchId", cartItem.getBranchId());
        cartItemMap.put("timestamp", cartItem.getTimestamp());
        cartItemMap.put("userId", userId);

        // Add selected options
        List<Map<String, Object>> optionsList = new ArrayList<>();
        for (CustomizationOption option : cartItem.getSelectedOptions()) {
            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put("name", option.getName());
            optionMap.put("price", option.getPrice());
            optionsList.add(optionMap);
        }
        cartItemMap.put("selectedOptions", optionsList);

        // Add to Firestore
        db.collection("cartItems")
                .add(cartItemMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Cart item added with ID: " + documentReference.getId());
                        Toast.makeText(FoodDetailsActivity.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show();

                        // Optional: Navigate back or to cart
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.w(TAG, "Error adding cart item", e);
                        Toast.makeText(FoodDetailsActivity.this, "Failed to add to cart. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Optional: Method to update existing cart item instead of adding new one
    private void updateCartItemInFirestore(CartItem cartItem, String userId) {
        // First check if the same product with same options already exists in cart
        db.collection("cartItems")
                .whereEqualTo("userId", userId)
                .whereEqualTo("productId", cartItem.getProductId())
                .whereEqualTo("branchId", cartItem.getBranchId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Update existing item
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("quantity", FieldValue.increment(cartItem.getQuantity()));
                        updates.put("totalPrice", FieldValue.increment(cartItem.getTotalPrice()));
                        updates.put("timestamp", System.currentTimeMillis());

                        db.collection("cartItems")
                                .document(document.getId())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cart item updated successfully");
                                    Toast.makeText(FoodDetailsActivity.this, "Cart updated successfully!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error updating cart item", e);
                                    Toast.makeText(FoodDetailsActivity.this, "Failed to update cart. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Add new item
                        storeCartItemInFirestore(cartItem, userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error checking existing cart items", e);
                    storeCartItemInFirestore(cartItem, userId);
                });
    }
}