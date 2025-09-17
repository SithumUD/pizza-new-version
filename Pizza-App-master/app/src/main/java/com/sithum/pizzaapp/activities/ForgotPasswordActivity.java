package com.sithum.pizzaapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.sithum.pizzaapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etEmail;
    private Button btnReset;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnReset);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Sending reset link to your email");
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        // Back button click listener
        btnBack.setOnClickListener(v -> finish());

        // Reset button click listener
        btnReset.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        // Validate email
        if (!validateEmail(email)) {
            return;
        }

        // Show progress dialog
        progressDialog.show();

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        // Reset email sent successfully
                        showSuccessDialog(email);
                    } else {
                        // Handle error
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error occurred";
                        handleResetError(errorMessage);
                    }
                });
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Clear any previous error
        etEmail.setError(null);
        return true;
    }

    private void showSuccessDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Link Sent")
                .setMessage("A password reset link has been sent to " + email +
                        ". Please check your email and follow the instructions to reset your password.")
                .setIcon(R.drawable.emailicon) // Add this icon to your drawable folder
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Close the activity
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleResetError(String errorMessage) {
        String userFriendlyMessage;

        // Convert Firebase error messages to user-friendly messages
        if (errorMessage.contains("user-not-found") || errorMessage.contains("invalid-email")) {
            userFriendlyMessage = "No account found with this email address. Please check your email and try again.";
        } else if (errorMessage.contains("network-request-failed")) {
            userFriendlyMessage = "Network error. Please check your internet connection and try again.";
        } else if (errorMessage.contains("too-many-requests")) {
            userFriendlyMessage = "Too many reset attempts. Please try again later.";
        } else {
            userFriendlyMessage = "Failed to send reset email. Please try again.";
        }

        showErrorDialog(userFriendlyMessage);
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(message)
                .setIcon(R.drawable.error) // Add this icon to your drawable folder
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Retry", (dialog, which) -> {
                    dialog.dismiss();
                    // Focus back to email field for retry
                    etEmail.requestFocus();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        // Check if progress dialog is showing
        if (progressDialog != null && progressDialog.isShowing()) {
            Toast.makeText(this, "Please wait for the current operation to complete", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss progress dialog to prevent memory leaks
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}