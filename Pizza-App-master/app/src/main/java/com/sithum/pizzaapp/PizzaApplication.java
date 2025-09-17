package com.sithum.pizzaapp;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class PizzaApplication extends Application {

    private static final String TAG = "PizzaApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        initializeCloudinary();
    }

    private void initializeCloudinary() {
        try {
            // Replace with your actual Cloudinary credentials
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dg0eycgai");
            config.put("api_key", "466484682685249");
            config.put("api_secret", "EkEUMGIGOsxopBHr3MPefimL2tE");

            // Optional: Set secure URLs (recommended for production)
            config.put("secure", "true");

            MediaManager.init(this, config);
            Log.d(TAG, "Cloudinary initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Cloudinary", e);
        }
    }
}