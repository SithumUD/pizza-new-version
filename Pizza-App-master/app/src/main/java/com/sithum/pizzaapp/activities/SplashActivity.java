package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sithum.pizzaapp.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("PizzaAppPrefs", MODE_PRIVATE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserLoginStatus();
        }, 3000);
    }

    private void checkUserLoginStatus() {
        // Check Firebase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Check SharedPreferences for login status (backup check)
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        Intent intent;

        if (currentUser != null && isLoggedIn) {
            // User is logged in, navigate to Home
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            // User is not logged in, navigate to Onboarding
            intent = new Intent(SplashActivity.this, OnboardActivity.class);
        }

        startActivity(intent);
        finish();
    }
}