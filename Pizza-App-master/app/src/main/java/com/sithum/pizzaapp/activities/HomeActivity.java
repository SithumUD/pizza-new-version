package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.FoodAdapter;
import com.sithum.pizzaapp.models.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements FoodAdapter.OnItemClickListener {

    private FrameLayout btnCart, btnProfile;
    private RecyclerView rvFood;
    private FoodAdapter foodAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // Category buttons
    private Button btnAll, btnClassic, btnSpicy, btnDesserts, btnDrinks;
    String branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        branchId = intent.getStringExtra("branchId");

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initViews();

        // Set up RecyclerView
        setupRecyclerView();

        // Set up click listeners
        setupClickListeners();

        // Load products from Firestore
        loadProducts("All");
    }

    private void initViews() {
        btnCart = findViewById(R.id.btncart);
        btnProfile = findViewById(R.id.btnprofile);
        rvFood = findViewById(R.id.rvFood);

        // Initialize category buttons
        btnAll = findViewById(R.id.btnAll);
        btnClassic = findViewById(R.id.btnClassic);
        btnSpicy = findViewById(R.id.btnSpicy);
        btnDesserts = findViewById(R.id.btnDesserts);
        btnDrinks = findViewById(R.id.btnDrinks);
    }

    private void setupRecyclerView() {
        rvFood.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter(this, productList, this);
        rvFood.setAdapter(foodAdapter);
    }

    private void setupClickListeners() {
        // Cart button click listener
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCartActivity();
            }
        });

        // Profile button click listener
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToProfileActivity();
            }
        });

        // Category button listeners
        btnAll.setOnClickListener(v -> loadProducts("All"));
        btnClassic.setOnClickListener(v -> loadProducts("Classic"));
        btnSpicy.setOnClickListener(v -> loadProducts("Spicy"));
        btnDesserts.setOnClickListener(v -> loadProducts("Desserts"));
        btnDrinks.setOnClickListener(v -> loadProducts("Drinks"));
    }

    private void loadProducts(String category) {
        if (category.equals("All")) {
            db.collection("products")
                    .whereEqualTo("status", "Available")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                productList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Product product = mapDocumentToProduct(document);
                                    productList.add(product);
                                }
                                foodAdapter.notifyDataSetChanged();
                            } else {
                                Log.w("HomeActivity", "Error getting documents.", task.getException());
                                Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            db.collection("products")
                    .whereEqualTo("category", category)
                    .whereEqualTo("status", "Available")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                productList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Product product = mapDocumentToProduct(document);
                                    productList.add(product);
                                }
                                foodAdapter.notifyDataSetChanged();
                            } else {
                                Log.w("HomeActivity", "Error getting documents.", task.getException());
                                Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private Product mapDocumentToProduct(QueryDocumentSnapshot document) {
        Product product = new Product();
        product.setId(document.getId());

        // Set basic fields
        if (document.contains("name")) {
            product.setName(document.getString("name"));
        }
        if (document.contains("description")) {
            product.setDescription(document.getString("description"));
        }
        if (document.contains("price")) {
            Object priceObj = document.get("price");
            if (priceObj instanceof Long) {
                product.setPrice(((Long) priceObj).doubleValue());
            } else if (priceObj instanceof Double) {
                product.setPrice((Double) priceObj);
            }
        }
        if (document.contains("category")) {
            product.setCategory(document.getString("category"));
        }
        if (document.contains("status")) {
            product.setStatus(document.getString("status"));
        }
        if (document.contains("imageUrl")) {
            product.setImageUrl(document.getString("imageUrl"));
        }

        return product;
    }

    @Override
    public void onItemClick(Product product) {
        // Navigate to FoodDetailsActivity when item is clicked
        navigateToFoodDetailsActivity(product);
    }

    @Override
    public void onAddToCartClick(Product product) {
        // This method is no longer used for adding to cart directly
        // Now we navigate to details page first
        navigateToFoodDetailsActivity(product);
    }

    private void navigateToFoodDetailsActivity(Product product) {
        Intent intent = new Intent(HomeActivity.this, FoodDetailsActivity.class);
        intent.putExtra("product", product.getId());
        intent.putExtra("branch", branchId);
        startActivity(intent);
    }

    private void navigateToCartActivity() {
        Intent intent = new Intent(HomeActivity.this, CartActivity.class);
        startActivity(intent);
    }

    private void navigateToProfileActivity() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}