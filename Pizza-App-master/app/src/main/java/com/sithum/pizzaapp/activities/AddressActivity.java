package com.sithum.pizzaapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.AddressAdapter;
import com.sithum.pizzaapp.models.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressActionListener {

    // UI Components
    private ScrollView layoutAddressList;
    private LinearLayout layoutAddressForm;
    private RecyclerView addressRecyclerView;
    private ImageView btnBack;
    private Button btnAddAddress, btnSaveAddress, btnCancelForm;
    private EditText etAddressName, etFullAddress;
    private CheckBox cbDefault;
    private TextView tvFormTitle;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    // Data and Adapter
    private List<Address> addressList;
    private AddressAdapter addressAdapter;
    private Address editingAddress = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeFirebase();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadAddresses();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        layoutAddressList = findViewById(R.id.layoutAddressList);
        layoutAddressForm = findViewById(R.id.layoutAddressForm);
        addressRecyclerView = findViewById(R.id.addresslist);
        btnBack = findViewById(R.id.btnBack);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
        btnCancelForm = findViewById(R.id.btnCancelForm);
        etAddressName = findViewById(R.id.etAddressName);
        etFullAddress = findViewById(R.id.etFullAddress);
        cbDefault = findViewById(R.id.cbDefault);
        tvFormTitle = findViewById(R.id.tvFormTitle);

        addressList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        addressAdapter = new AddressAdapter(addressList, this);
        addressRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressRecyclerView.setAdapter(addressAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddAddress.setOnClickListener(v -> showAddressForm(false, null));

        btnSaveAddress.setOnClickListener(v -> {
            if (validateForm()) {
                if (isEditMode) {
                    updateAddress();
                } else {
                    saveNewAddress();
                }
            }
        });

        btnCancelForm.setOnClickListener(v -> showAddressList());
    }

    // AddressAdapter.OnAddressActionListener implementation
    @Override
    public void onEditAddress(Address address) {
        showAddressForm(true, address);
    }

    @Override
    public void onDeleteAddress(Address address) {
        showDeleteConfirmation(address);
    }

    @Override
    public void onSetAsDefault(Address address) {
        setAddressAsDefault(address);
    }

    private void showAddressForm(boolean editMode, Address address) {
        this.isEditMode = editMode;
        this.editingAddress = address;

        layoutAddressList.setVisibility(View.GONE);
        layoutAddressForm.setVisibility(View.VISIBLE);

        if (editMode && address != null) {
            tvFormTitle.setText("Edit Address");
            btnSaveAddress.setText("Update Address");
            etAddressName.setText(address.getName());
            etFullAddress.setText(address.getFullAddress());
            cbDefault.setChecked(address.isDefault());
        } else {
            tvFormTitle.setText("Add New Address");
            btnSaveAddress.setText("Add Address");
            etAddressName.setText("");
            etFullAddress.setText("");
            cbDefault.setChecked(false);
        }
    }

    private void showAddressList() {
        layoutAddressList.setVisibility(View.VISIBLE);
        layoutAddressForm.setVisibility(View.GONE);
        editingAddress = null;
        isEditMode = false;
    }

    private boolean validateForm() {
        String name = etAddressName.getText().toString().trim();
        String fullAddress = etFullAddress.getText().toString().trim();

        if (name.isEmpty()) {
            etAddressName.setError("Address name is required");
            etAddressName.requestFocus();
            return false;
        }

        if (fullAddress.isEmpty()) {
            etFullAddress.setError("Full address is required");
            etFullAddress.requestFocus();
            return false;
        }

        return true;
    }

    private void saveNewAddress() {
        String name = etAddressName.getText().toString().trim();
        String fullAddress = etFullAddress.getText().toString().trim();
        boolean isDefault = cbDefault.isChecked();

        Address newAddress = new Address();
        newAddress.setName(name);
        newAddress.setFullAddress(fullAddress);
        newAddress.setDefault(isDefault);
        newAddress.setUserId(currentUserId);

        if (isDefault) {
            // First, remove default status from all existing addresses
            updateExistingDefaultAddresses(() -> {
                // Then save the new address
                saveAddressToFirestore(newAddress);
            });
        } else {
            saveAddressToFirestore(newAddress);
        }
    }

    private void updateAddress() {
        String name = etAddressName.getText().toString().trim();
        String fullAddress = etFullAddress.getText().toString().trim();
        boolean isDefault = cbDefault.isChecked();

        editingAddress.setName(name);
        editingAddress.setFullAddress(fullAddress);
        editingAddress.setDefault(isDefault);

        if (isDefault) {
            // First, remove default status from all other addresses
            updateExistingDefaultAddresses(() -> {
                // Then update this address
                updateAddressInFirestore(editingAddress);
            });
        } else {
            updateAddressInFirestore(editingAddress);
        }
    }

    private void setAddressAsDefault(Address address) {
        // First, remove default status from all existing addresses
        updateExistingDefaultAddresses(() -> {
            // Then set this address as default
            address.setDefault(true);
            updateAddressInFirestore(address);
        });
    }

    private void updateExistingDefaultAddresses(Runnable onComplete) {
        db.collection("addresses")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("isDefault", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        onComplete.run();
                        return;
                    }

                    int totalUpdates = queryDocumentSnapshots.size();
                    final int[] completedUpdates = {0};

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // Skip the current editing address if we're in edit mode
                        if (isEditMode && editingAddress != null &&
                                doc.getId().equals(editingAddress.getId())) {
                            completedUpdates[0]++;
                            if (completedUpdates[0] == totalUpdates) {
                                onComplete.run();
                            }
                            continue;
                        }

                        db.collection("addresses")
                                .document(doc.getId())
                                .update("isDefault", false)
                                .addOnSuccessListener(aVoid -> {
                                    completedUpdates[0]++;
                                    if (completedUpdates[0] == totalUpdates) {
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddressActivity.this,
                                            "Error updating default addresses", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking default addresses", Toast.LENGTH_SHORT).show();
                    onComplete.run(); // Continue anyway
                });
    }

    private void saveAddressToFirestore(Address address) {
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", address.getName());
        addressData.put("fullAddress", address.getFullAddress());
        addressData.put("isDefault", address.isDefault());
        addressData.put("userId", address.getUserId());
        addressData.put("timestamp", System.currentTimeMillis());

        db.collection("addresses")
                .add(addressData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Address added successfully", Toast.LENGTH_SHORT).show();
                    showAddressList();
                    loadAddresses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add address: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAddressInFirestore(Address address) {
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", address.getName());
        addressData.put("fullAddress", address.getFullAddress());
        addressData.put("isDefault", address.isDefault());

        db.collection("addresses")
                .document(address.getId())
                .update(addressData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show();
                    showAddressList();
                    loadAddresses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update address: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAddresses() {
        db.collection("addresses")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Address address = new Address();
                        address.setId(doc.getId());
                        address.setName(doc.getString("name"));
                        address.setFullAddress(doc.getString("fullAddress"));
                        address.setDefault(Boolean.TRUE.equals(doc.getBoolean("isDefault")));
                        address.setUserId(doc.getString("userId"));

                        addressList.add(address);
                    }

                    // Notify adapter that data has changed
                    addressAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load addresses: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("AddressLoadError", "Failed to load addresses", e);
                });
    }

    private void showDeleteConfirmation(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAddress(address))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAddress(Address address) {
        db.collection("addresses")
                .document(address.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Address deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete address: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}