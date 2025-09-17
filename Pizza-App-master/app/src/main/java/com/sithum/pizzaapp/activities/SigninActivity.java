package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.activities.admin.DashboardActivity;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Signup Activity
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            // Show progress dialog if needed
            // ProgressDialog progressDialog = new ProgressDialog(this);
            // progressDialog.setMessage("Signing in...");
            // progressDialog.show();

            // Sign in user with email and password
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, check if email is verified
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    if (user.isEmailVerified()) {
                                        // Email is verified, check user role and navigate accordingly
                                        checkUserRoleAndNavigate(user.getUid());
                                    } else {
                                        // Email is not verified
                                        Toast.makeText(SigninActivity.this,
                                                "Please verify your email address before signing in.",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut(); // Sign out until email is verified
                                    }
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                String errorMessage = "Authentication failed.";

                                if (task.getException() != null) {
                                    errorMessage = task.getException().getMessage();
                                }

                                Toast.makeText(SigninActivity.this, errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }

                            // Hide progress dialog if needed
                            // progressDialog.dismiss();
                        }
                    });
        }
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void checkUserRoleAndNavigate(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Get user role, default to "customer" if not found
                                String role = document.getString("role");
                                if (role == null) {
                                    role = "customer";
                                }

                                // Navigate based on role
                                navigateBasedOnRole(role);
                            } else {
                                // User document doesn't exist, create one with default role
                                createUserDocument(userId);
                            }
                        } else {
                            Log.w(TAG, "Error getting user document", task.getException());
                            // Navigate to customer screen as fallback
                            navigateToCustomerScreen();
                        }
                    }
                });
    }

    private void navigateBasedOnRole(String role) {
        switch (role.toLowerCase()) {
            case "admin":
                navigateToAdminScreen();
                break;
            case "staff":
                navigateToStaffScreen();
                break;
            case "customer":
            default:
                navigateToCustomerScreen();
                break;
        }
    }

    private void navigateToAdminScreen() {
        Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SigninActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToStaffScreen() {
        Toast.makeText(this, "Welcome Staff!", Toast.LENGTH_SHORT).show();
        // For staff, you can navigate to a different activity or same as customer
        Intent intent = new Intent(SigninActivity.this, LocationActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToCustomerScreen() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SigninActivity.this, LocationActivity.class);
        startActivity(intent);
        finish();
    }

    private void createUserDocument(String userId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Create a new user document with default role
            java.util.Map<String, Object> userData = new java.util.HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("role", "customer");
            userData.put("createdAt", System.currentTimeMillis());

            db.collection("users")
                    .document(userId)
                    .set(userData)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User document created with default role");
                                navigateToCustomerScreen();
                            } else {
                                Log.w(TAG, "Error creating user document", task.getException());
                                navigateToCustomerScreen(); // Navigate anyway as fallback
                            }
                        }
                    });
        }
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required to reset password");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this,
                                    "Password reset email sent. Check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SigninActivity.this,
                                    "Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is already signed in and email is verified, navigate based on role
            checkUserRoleAndNavigate(currentUser.getUid());
        }
    }
}