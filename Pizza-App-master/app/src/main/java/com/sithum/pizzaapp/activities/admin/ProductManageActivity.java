package com.sithum.pizzaapp.activities.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.CustomizationOptionAdapter;
import com.sithum.pizzaapp.adapters.ProductManageAdapter;
import com.sithum.pizzaapp.models.CustomizationOption;
import com.sithum.pizzaapp.models.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductManageActivity extends AppCompatActivity implements
        ProductManageAdapter.OnProductActionListener {

    private static final String TAG = "ProductManageActivity";
    private static final String COLLECTION_PRODUCTS = "products";

    private RecyclerView productsRecyclerView;
    private ProductManageAdapter productAdapter;
    private TabLayout categoryTabs;
    private Button addProductButton;
    private ImageView backButton;
    private ProgressBar progressBar;

    private List<Product> productList;
    private List<Product> filteredProductList;
    private String currentCategory = "All";

    // Firebase
    private FirebaseFirestore db;

    // Dialog components
    private AlertDialog addEditDialog;
    private View dialogView;
    private TextInputEditText etProductName, etProductDescription, etProductPrice;
    private TextInputEditText etOptionName, etOptionPrice;
    private Spinner spinnerCategory;
    private ImageView ivProductImage;
    private RecyclerView rvCustomizationOptions;
    private CustomizationOptionAdapter optionAdapter;
    private List<CustomizationOption> customizationOptions;
    private ProgressBar dialogProgressBar;

    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isEditMode = false;
    private String editingProductId;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_manage);

        initializeFirebase();
        initializeCloudinary();
        initializeViews();
        setupImagePicker();
        setupRecyclerView();
        setupTabLayout();
        setupClickListeners();
        loadProducts();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void initializeCloudinary() {
        // Initialize Cloudinary with your configuration
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "your_cloud_name");
        config.put("api_key", "your_api_key");
        config.put("api_secret", "your_api_secret");

        try {
            MediaManager.init(this, config);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Cloudinary", e);
        }
    }

    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        categoryTabs = findViewById(R.id.categoryTabs);
        addProductButton = findViewById(R.id.addProductButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        customizationOptions = new ArrayList<>();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (ivProductImage != null) {
                            ivProductImage.setImageURI(selectedImageUri);
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductManageAdapter(filteredProductList, this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void setupTabLayout() {
        categoryTabs.addTab(categoryTabs.newTab().setText("All"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Classic"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Spicy"));
        categoryTabs.addTab(categoryTabs.newTab().setText("Desserts"));

        categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                filterProductsByCategory(currentCategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        addProductButton.setOnClickListener(v -> {
            isEditMode = false;
            editingProductId = null;
            showAddEditProductDialog(null);
        });
    }

    private void loadProducts() {
        showProgress(true);

        db.collection(COLLECTION_PRODUCTS)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Product product = documentToProduct(document);
                            if (product != null) {
                                productList.add(product);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing product: " + document.getId(), e);
                        }
                    }

                    filterProductsByCategory(currentCategory);
                    showProgress(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products", e);
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    showProgress(false);
                });
    }

    private Product documentToProduct(DocumentSnapshot document) {
        try {
            String id = document.getId();
            String name = document.getString("name");
            String description = document.getString("description");
            Double price = document.getDouble("price");
            String category = document.getString("category");
            String status = document.getString("status");
            String imageUrl = document.getString("imageUrl");

            // Parse customization options
            List<CustomizationOption> options = new ArrayList<>();
            List<Map<String, Object>> optionMaps = (List<Map<String, Object>>) document.get("customizationOptions");

            if (optionMaps != null) {
                for (Map<String, Object> optionMap : optionMaps) {
                    String optionName = (String) optionMap.get("name");
                    Double optionPrice = (Double) optionMap.get("price");
                    if (optionName != null && optionPrice != null) {
                        options.add(new CustomizationOption(optionName, optionPrice));
                    }
                }
            }

            Product product = new Product(id, name, description,
                    price != null ? price : 0.0, category, status,
                    R.drawable.pizza, options);
            product.setImageUrl(imageUrl);

            return product;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to product", e);
            return null;
        }
    }

    private void filterProductsByCategory(String category) {
        filteredProductList.clear();
        if (category.equals("All")) {
            filteredProductList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getCategory().equals(category)) {
                    filteredProductList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private void showAddEditProductDialog(Product product) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            dialogView = inflater.inflate(R.layout.dialog_add_edit_product, null);

            if (dialogView == null) {
                Toast.makeText(this, "Error creating dialog", Toast.LENGTH_SHORT).show();
                return;
            }

            initializeDialogViews(dialogView);
            setupCustomizationOptionsRecyclerView();

            if (product != null) {
                populateDialogWithProduct(product);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            addEditDialog = builder.create();

            if (addEditDialog.getWindow() != null) {
                addEditDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
            }

            addEditDialog.show();
            setupDialogClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog", e);
            Toast.makeText(this, "Error showing dialog", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeDialogViews(View dialogView) {
        try {
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            etProductName = dialogView.findViewById(R.id.etProductName);
            etProductDescription = dialogView.findViewById(R.id.etProductDescription);
            etProductPrice = dialogView.findViewById(R.id.etProductPrice);
            etOptionName = dialogView.findViewById(R.id.etOptionName);
            etOptionPrice = dialogView.findViewById(R.id.etOptionPrice);
            spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
            ivProductImage = dialogView.findViewById(R.id.ivProductImage);
            rvCustomizationOptions = dialogView.findViewById(R.id.rvCustomizationOptions);
            dialogProgressBar = dialogView.findViewById(R.id.dialogProgressBar);

            if (dialogTitle != null) {
                dialogTitle.setText(isEditMode ? "Edit Product" : "Add Product");
            }

            // Setup category spinner
            if (spinnerCategory != null) {
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"Classic", "Spicy", "Desserts"});
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
            }

            customizationOptions.clear();
            selectedImageUri = null;
            uploadedImageUrl = null;

        } catch (Exception e) {
            Log.e(TAG, "Error initializing dialog views", e);
        }
    }

    private void setupCustomizationOptionsRecyclerView() {
        try {
            if (rvCustomizationOptions != null) {
                optionAdapter = new CustomizationOptionAdapter(customizationOptions,
                        option -> removeCustomizationOption(option));
                rvCustomizationOptions.setLayoutManager(new LinearLayoutManager(this));
                rvCustomizationOptions.setAdapter(optionAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up customization options RecyclerView", e);
        }
    }

    private void setupDialogClickListeners() {
        try {
            if (dialogView == null) return;

            Button btnCancel = dialogView.findViewById(R.id.btnCancel);
            Button btnSave = dialogView.findViewById(R.id.btnSave);
            Button btnAddOption = dialogView.findViewById(R.id.btnAddOption);
            TextView tvChangeImage = dialogView.findViewById(R.id.tvChangeImage);

            if (btnCancel != null) {
                btnCancel.setOnClickListener(v -> {
                    if (addEditDialog != null && addEditDialog.isShowing()) {
                        addEditDialog.dismiss();
                    }
                });
            }

            if (btnSave != null) {
                btnSave.setOnClickListener(v -> saveProduct());
            }

            if (btnAddOption != null) {
                btnAddOption.setOnClickListener(v -> addCustomizationOption());
            }

            if (tvChangeImage != null) {
                tvChangeImage.setOnClickListener(v -> openImagePicker());
            }

            if (ivProductImage != null) {
                ivProductImage.setOnClickListener(v -> openImagePicker());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up dialog click listeners", e);
        }
    }

    private void populateDialogWithProduct(Product product) {
        try {
            if (etProductName != null) {
                etProductName.setText(product.getName());
            }
            if (etProductDescription != null) {
                etProductDescription.setText(product.getDescription());
            }
            if (etProductPrice != null) {
                etProductPrice.setText(String.valueOf(product.getPrice()));
            }

            // Set category spinner selection
            if (spinnerCategory != null) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(product.getCategory());
                    spinnerCategory.setSelection(position);
                }
            }

            // Load product image
            if (ivProductImage != null) {
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Glide.with(this)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.pizza)
                            .error(R.drawable.pizza)
                            .into(ivProductImage);
                    uploadedImageUrl = product.getImageUrl();
                } else {
                    ivProductImage.setImageResource(product.getImageResource());
                }
            }

            // Load customization options
            customizationOptions.clear();
            customizationOptions.addAll(product.getCustomizationOptions());
            if (optionAdapter != null) {
                optionAdapter.notifyDataSetChanged();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error populating dialog with product data", e);
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image picker", e);
            Toast.makeText(this, "Error opening image picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCustomizationOption() {
        try {
            String optionName = etOptionName != null ? etOptionName.getText().toString().trim() : "";
            String optionPriceStr = etOptionPrice != null ? etOptionPrice.getText().toString().trim() : "";

            if (optionName.isEmpty() || optionPriceStr.isEmpty()) {
                Toast.makeText(this, "Please enter option name and price", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(optionPriceStr);
            CustomizationOption option = new CustomizationOption(optionName, price);
            customizationOptions.add(option);

            if (optionAdapter != null) {
                optionAdapter.notifyDataSetChanged();
            }

            // Clear input fields
            if (etOptionName != null) {
                etOptionName.setText("");
            }
            if (etOptionPrice != null) {
                etOptionPrice.setText("");
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error adding customization option", e);
            Toast.makeText(this, "Error adding option", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeCustomizationOption(CustomizationOption option) {
        try {
            customizationOptions.remove(option);
            if (optionAdapter != null) {
                optionAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing customization option", e);
        }
    }

    private void saveProduct() {
        try {
            String name = etProductName != null ? etProductName.getText().toString().trim() : "";
            String description = etProductDescription != null ? etProductDescription.getText().toString().trim() : "";
            String priceStr = etProductPrice != null ? etProductPrice.getText().toString().trim() : "";
            String category = spinnerCategory != null && spinnerCategory.getSelectedItem() != null ?
                    spinnerCategory.getSelectedItem().toString() : "Classic";

            if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);

            // Show progress
            showDialogProgress(true);

            // If image is selected, upload to Cloudinary first
            if (selectedImageUri != null) {
                uploadImageToCloudinary(name, description, price, category);
            } else {
                // Save without new image
                saveProductToFirestore(name, description, price, category, uploadedImageUrl);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving product", e);
            Toast.makeText(this, "Error saving product", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToCloudinary(String name, String description, double price, String category) {
        try {
            String publicId = "products/" + System.currentTimeMillis();

            MediaManager.get().upload(selectedImageUri)
                    .option("public_id", publicId)
                    .option("folder", "pizza_app/products")
                    .option("transformation", "c_fill,w_400,h_400,q_auto")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "Upload started: " + requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Update progress if needed
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            try {
                                uploadedImageUrl = (String) resultData.get("secure_url");
                                Log.d(TAG, "Image uploaded successfully: " + uploadedImageUrl);

                                runOnUiThread(() -> {
                                    saveProductToFirestore(name, description, price, category, uploadedImageUrl);
                                });

                            } catch (Exception e) {
                                Log.e(TAG, "Error processing upload result", e);
                                runOnUiThread(() -> {
                                    showDialogProgress(false);
                                    Toast.makeText(ProductManageActivity.this,
                                            "Error processing image upload", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Cloudinary upload error: " + error.getDescription());
                            runOnUiThread(() -> {
                                showDialogProgress(false);
                                Toast.makeText(ProductManageActivity.this,
                                        "Failed to upload image: " + error.getDescription(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();

        } catch (Exception e) {
            Log.e(TAG, "Error starting Cloudinary upload", e);
            showDialogProgress(false);
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProductToFirestore(String name, String description, double price,
                                        String category, String imageUrl) {
        try {
            // Convert customization options to Map format
            List<Map<String, Object>> optionMaps = new ArrayList<>();
            for (CustomizationOption option : customizationOptions) {
                Map<String, Object> optionMap = new HashMap<>();
                optionMap.put("name", option.getName());
                optionMap.put("price", option.getPrice());
                optionMaps.add(optionMap);
            }

            Map<String, Object> productData = new HashMap<>();
            productData.put("name", name);
            productData.put("description", description);
            productData.put("price", price);
            productData.put("category", category);
            productData.put("status", "Available");
            productData.put("customizationOptions", optionMaps);
            productData.put("createdAt", System.currentTimeMillis());
            productData.put("updatedAt", System.currentTimeMillis());

            if (imageUrl != null && !imageUrl.isEmpty()) {
                productData.put("imageUrl", imageUrl);
            }

            if (isEditMode && editingProductId != null) {
                // Update existing product
                db.collection(COLLECTION_PRODUCTS)
                        .document(editingProductId)
                        .update(productData)
                        .addOnSuccessListener(aVoid -> {
                            showDialogProgress(false);
                            Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();

                            if (addEditDialog != null && addEditDialog.isShowing()) {
                                addEditDialog.dismiss();
                            }

                            loadProducts(); // Refresh the list
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating product", e);
                            showDialogProgress(false);
                            Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Add new product
                db.collection(COLLECTION_PRODUCTS)
                        .add(productData)
                        .addOnSuccessListener(documentReference -> {
                            showDialogProgress(false);
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();

                            if (addEditDialog != null && addEditDialog.isShowing()) {
                                addEditDialog.dismiss();
                            }

                            loadProducts(); // Refresh the list
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding product", e);
                            showDialogProgress(false);
                            Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving product to Firestore", e);
            showDialogProgress(false);
            Toast.makeText(this, "Error saving product", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showDialogProgress(boolean show) {
        if (dialogProgressBar != null) {
            dialogProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onEditProduct(Product product, int position) {
        isEditMode = true;
        editingProductId = product.getId();
        showAddEditProductDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product, int position) {
        showDeleteConfirmationDialog(product);
    }

    @Override
    public void onProductClick(Product product, int position) {
        showProductDetailBottomSheet(product);
    }

    private void showDeleteConfirmationDialog(Product product) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this product? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete confirmation dialog", e);
        }
    }

    private void deleteProduct(Product product) {
        try {
            showProgress(true);

            db.collection(COLLECTION_PRODUCTS)
                    .document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        showProgress(false);
                        Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                        loadProducts(); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting product", e);
                        showProgress(false);
                        Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product", e);
            Toast.makeText(this, "Error deleting product", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProductDetailBottomSheet(Product product) {
        try {
            BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
            View bottomSheetView = LayoutInflater.from(this)
                    .inflate(R.layout.bottom_sheet_product_detail, null);

            // Initialize bottom sheet views
            ImageView ivProductImage = bottomSheetView.findViewById(R.id.ivProductImage);
            TextView tvProductName = bottomSheetView.findViewById(R.id.tvProductName);
            TextView tvProductDescription = bottomSheetView.findViewById(R.id.tvProductDescription);
            TextView tvProductPrice = bottomSheetView.findViewById(R.id.tvProductPrice);
            TextView tvProductCategory = bottomSheetView.findViewById(R.id.tvProductCategory);
            TextView tvProductStatus = bottomSheetView.findViewById(R.id.tvProductStatus);
            RecyclerView rvCustomizationOptions = bottomSheetView.findViewById(R.id.rvCustomizationOptions);

            Button btnMarkUnavailable = bottomSheetView.findViewById(R.id.btnMarkUnavailable);
            Button btnEditProduct = bottomSheetView.findViewById(R.id.btnEditProduct);
            ImageView btnDeleteProduct = bottomSheetView.findViewById(R.id.btnDeleteProduct);
            ImageView btnClose = bottomSheetView.findViewById(R.id.btnClose);

            // Populate data with null checks
            if (ivProductImage != null) {
                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Glide.with(this)
                            .load(product.getImageUrl())
                            .placeholder(R.drawable.pizza)
                            .error(R.drawable.pizza)
                            .into(ivProductImage);
                } else {
                    ivProductImage.setImageResource(product.getImageResource());
                }
            }

            if (tvProductName != null) {
                tvProductName.setText(product.getName());
            }
            if (tvProductDescription != null) {
                tvProductDescription.setText(product.getDescription());
            }
            if (tvProductPrice != null) {
                tvProductPrice.setText("LKR " + String.format("%.0f", product.getPrice()));
            }
            if (tvProductCategory != null) {
                tvProductCategory.setText(product.getCategory());
            }
            if (tvProductStatus != null) {
                tvProductStatus.setText(product.getStatus());
                // Update button text based on status
                if (btnMarkUnavailable != null) {
                    btnMarkUnavailable.setText("Available".equals(product.getStatus()) ?
                            "Mark Unavailable" : "Mark Available");
                }
            }

            // Setup customization options
            if (rvCustomizationOptions != null) {
                CustomizationOptionAdapter displayAdapter = new CustomizationOptionAdapter(
                        product.getCustomizationOptions(), null);
                rvCustomizationOptions.setLayoutManager(new LinearLayoutManager(this));
                rvCustomizationOptions.setAdapter(displayAdapter);
            }

            // Setup click listeners
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> bottomSheet.dismiss());
            }

            if (btnEditProduct != null) {
                btnEditProduct.setOnClickListener(v -> {
                    bottomSheet.dismiss();
                    onEditProduct(product, -1);
                });
            }

            if (btnDeleteProduct != null) {
                btnDeleteProduct.setOnClickListener(v -> {
                    bottomSheet.dismiss();
                    showDeleteConfirmationDialog(product);
                });
            }

            if (btnMarkUnavailable != null) {
                btnMarkUnavailable.setOnClickListener(v -> {
                    toggleProductStatus(product);
                    bottomSheet.dismiss();
                });
            }

            bottomSheet.setContentView(bottomSheetView);
            bottomSheet.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing product detail bottom sheet", e);
            Toast.makeText(this, "Error showing product details", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleProductStatus(Product product) {
        try {
            showProgress(true);

            String newStatus = "Available".equals(product.getStatus()) ? "Unavailable" : "Available";

            db.collection(COLLECTION_PRODUCTS)
                    .document(product.getId())
                    .update("status", newStatus, "updatedAt", System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> {
                        showProgress(false);
                        Toast.makeText(this, "Product status updated successfully", Toast.LENGTH_SHORT).show();
                        loadProducts(); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating product status", e);
                        showProgress(false);
                        Toast.makeText(this, "Failed to update product status", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error toggling product status", e);
            Toast.makeText(this, "Error updating product status", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (addEditDialog != null && addEditDialog.isShowing()) {
                addEditDialog.dismiss();
                addEditDialog = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        super.onDestroy();
    }
}