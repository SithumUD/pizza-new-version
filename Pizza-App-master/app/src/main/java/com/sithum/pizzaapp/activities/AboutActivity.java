package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sithum.pizzaapp.R;

public class AboutActivity extends AppCompatActivity {

    // UI Components
    private ImageView backButton;
    private ImageView facebookIcon, instagramIcon, twitterIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header
        backButton = findViewById(R.id.backButton);

        // Social Media Icons
        facebookIcon = findViewById(R.id.facebookIcon);
        instagramIcon = findViewById(R.id.instagramIcon);
        twitterIcon = findViewById(R.id.twitterIcon);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Social Media Click Listeners
        facebookIcon.setOnClickListener(v -> openSocialMedia("https://www.facebook.com"));
        instagramIcon.setOnClickListener(v -> openSocialMedia("https://www.instagram.com"));
        twitterIcon.setOnClickListener(v -> openSocialMedia("https://www.twitter.com"));
    }

    private void openSocialMedia(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}