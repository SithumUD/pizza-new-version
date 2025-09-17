package com.sithum.pizzaapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sithum.pizzaapp.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvUserName, tvUserEmail, tvUserPhone, tvEditProfile;
    private Button logoutBtn;

    // Menu item CardViews
    private CardView btnOrderHistory, btnMyAddresses, btnPaymentMethods,
            btnHelp, btnAbout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // User data variables
    private String currentFullName = "";
    private String currentEmail = "";
    private String currentPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        initViews();

        // Set up click listeners
        setupClickListeners();

        // Load user data
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.txtname);
        tvUserEmail = findViewById(R.id.txtemail);
        tvUserPhone = findViewById(R.id.txtphone);
        tvEditProfile = findViewById(R.id.tvEditProfile);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Initialize menu item CardViews
        btnOrderHistory = findViewById(R.id.btnorderhistory);
        btnMyAddresses = findViewById(R.id.btnmyaddresses);
        btnPaymentMethods = findViewById(R.id.btnpaymentmethods);
        btnHelp = findViewById(R.id.btnhelp);
        btnAbout = findViewById(R.id.btnabout);

        // Set default user data
        if (currentUser != null) {
            currentEmail = currentUser.getEmail();
            tvUserEmail.setText(currentEmail);
            currentFullName = currentUser.getDisplayName() != null ?
                    currentUser.getDisplayName() : "User";
            tvUserName.setText(currentFullName);
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Edit Profile
        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        // Order History
        btnOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, OrderTrackingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // My Addresses
        btnMyAddresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AddressActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Payment Methods
        btnPaymentMethods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PaymentMethodsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Help and Support
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HelpActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // About Us
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        // Logout button
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void loadUserData() {
        if (currentUser != null) {
            // Get user data from Firestore
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Update UI with user data
                                    currentFullName = document.getString("fullName");
                                    currentEmail = document.getString("email");
                                    currentPhone = document.getString("phone");

                                    if (currentFullName != null && !currentFullName.isEmpty()) {
                                        tvUserName.setText(currentFullName);
                                    }

                                    if (currentEmail != null && !currentEmail.isEmpty()) {
                                        tvUserEmail.setText(currentEmail);
                                    }

                                    if (currentPhone != null && !currentPhone.isEmpty()) {
                                        tvUserPhone.setText(currentPhone);
                                    } else {
                                        tvUserPhone.setText("No phone number");
                                    }
                                }
                            } else {
                                // Use auth data as fallback
                                if (currentUser.getDisplayName() != null) {
                                    currentFullName = currentUser.getDisplayName();
                                    tvUserName.setText(currentFullName);
                                }
                                currentEmail = currentUser.getEmail();
                                tvUserEmail.setText(currentEmail);
                                tvUserPhone.setText("No phone number");
                            }
                        }
                    });
        }
    }

    private void showEditProfileDialog() {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        // Get dialog views
        EditText etFullName = dialogView.findViewById(R.id.etFullName);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Set current values
        etFullName.setText(currentFullName);
        etEmail.setText(currentEmail);
        etPhone.setText(currentPhone);

        AlertDialog dialog = builder.create();

        // Save button click listener
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newFullName = etFullName.getText().toString().trim();
                String newEmail = etEmail.getText().toString().trim();
                String newPhone = etPhone.getText().toString().trim();

                // Validate input
                if (newFullName.isEmpty()) {
                    etFullName.setError("Full name is required");
                    return;
                }

                if (newEmail.isEmpty()) {
                    etEmail.setError("Email is required");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    etEmail.setError("Please enter a valid email address");
                    return;
                }

                // Update profile
                updateProfile(newFullName, newEmail, newPhone, dialog);
            }
        });

        // Cancel button click listener
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateProfile(String fullName, String email, String phone, AlertDialog dialog) {
        if (currentUser != null) {
            // Prepare user data
            Map<String, Object> userData = new HashMap<>();
            userData.put("fullName", fullName);
            userData.put("email", email);
            userData.put("phone", phone);
            userData.put("userId", currentUser.getUid());

            // Update Firestore document
            db.collection("users")
                    .document(currentUser.getUid())
                    .set(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Update local variables and UI
                            currentFullName = fullName;
                            currentEmail = email;
                            currentPhone = phone;

                            tvUserName.setText(fullName);
                            tvUserEmail.setText(email);
                            tvUserPhone.setText(phone.isEmpty() ? "No phone number" : phone);

                            Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to SigninActivity
        Intent intent = new Intent(ProfileActivity.this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Add animation if desired
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            // User is not signed in, redirect to login
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        }
    }
}