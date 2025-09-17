package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sithum.pizzaapp.R;

public class OnboardActivity extends AppCompatActivity {

    private Button btnStart;
    private static final String PREFS_NAME = "OnboardPrefs";
    private static final String PREF_FIRST_LAUNCH = "firstLaunch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if it's the first launch
        if (!isFirstLaunch()) {
            navigateToSigninActivity();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboard);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initViews();

        // Set click listener for the button
        setClickListener();
    }

    private void initViews() {
        btnStart = findViewById(R.id.btnstart);
    }

    private void setClickListener() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark that the user has seen the onboarding
                setFirstLaunchCompleted();

                // Navigate to SigninActivity
                navigateToSigninActivity();
            }
        });
    }

    private void navigateToSigninActivity() {
        Intent intent = new Intent(OnboardActivity.this, SigninActivity.class);
        startActivity(intent);

        // Optional: Add animation for smoother transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        finish();
    }

    private boolean isFirstLaunch() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return preferences.getBoolean(PREF_FIRST_LAUNCH, true);
    }

    private void setFirstLaunchCompleted() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_FIRST_LAUNCH, false);
        editor.apply();
    }
}