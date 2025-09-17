package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderTrackingActivity extends AppCompatActivity {

    private static final String TAG = "OrderTrackingActivity";

    // UI Components
    private ImageView backButton;
    private TextView orderNumber;
    private TextView estimatedTime;
    private TextView statusText;
    private TextView deliveryAddress;
    private TextView deliveryPartnerName;
    private CardView backToHomeButton;

    // Progress indicators
    private View orderReceivedDot, preparingDot, outForDeliveryDot, deliveredDot;
    private View progressBar1, progressBar2, progressBar3;
    private TextView orderReceivedText, preparingText, outForDeliveryText, deliveredText;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration orderListener;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get orderId from intent
        orderId = getIntent().getStringExtra("orderId");

        // Initialize UI components
        initViews();
        setupClickListeners();

        // Check if orderId exists, if not find the most recent order
        if (orderId == null || orderId.isEmpty()) {
            findMostRecentOrder();
        } else {
            startOrderTracking();
        }
    }

    private void findMostRecentOrder() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to track your orders", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        // Query to find the most recent order for the current user
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Get the most recent order
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            orderId = document.getId();
                            Log.d(TAG, "Found most recent order: " + orderId);

                            // Start tracking the found order
                            startOrderTracking();
                        } else {
                            Toast.makeText(this, "No orders found for your account", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Error getting recent order: ", task.getException());
                        Toast.makeText(this, "Failed to load order data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void initViews() {
        // Header
        backButton = findViewById(R.id.backButton);

        // Order info
        orderNumber = findViewById(R.id.orderNumber);
        estimatedTime = findViewById(R.id.estimatedTime);
        statusText = findViewById(R.id.statusText);

        // Progress dots (you'll need to add IDs to your XML)
        orderReceivedDot = findViewById(R.id.orderReceivedDot);
        preparingDot = findViewById(R.id.preparingDot);
        outForDeliveryDot = findViewById(R.id.outForDeliveryDot);
        deliveredDot = findViewById(R.id.deliveredDot);

        // Progress bars
        progressBar1 = findViewById(R.id.progressBar1);
        progressBar2 = findViewById(R.id.progressBar2);
        progressBar3 = findViewById(R.id.progressBar3);

        // Progress texts
        orderReceivedText = findViewById(R.id.orderReceivedText);
        preparingText = findViewById(R.id.preparingText);
        outForDeliveryText = findViewById(R.id.outForDeliveryText);
        deliveredText = findViewById(R.id.deliveredText);

        // Delivery info
        deliveryAddress = findViewById(R.id.deliveryAddress);
        deliveryPartnerName = findViewById(R.id.deliveryPartnerName);

        // Back to home button
        backToHomeButton = findViewById(R.id.backToHomeButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        backToHomeButton.setOnClickListener(v -> {
            // Navigate back to home
            Intent intent = new Intent(OrderTrackingActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startOrderTracking() {
        if (orderId == null || orderId.isEmpty()) {
            finish();
            return;
        }

        DocumentReference orderRef = db.collection("orders").document(orderId);

        orderListener = orderRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                updateOrderUI(documentSnapshot);
            } else {
                Log.d(TAG, "No such document");
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderUI(DocumentSnapshot document) {
        try {
            // Update order number
            String orderNumText = "Order #" + document.getId();
            orderNumber.setText(orderNumText);

            // Get order status
            String status = document.getString("status");
            if (status == null) status = "pending";

            // Update status and progress
            updateOrderStatus(status);

            // Update delivery address
            String address = document.getString("deliveryAddress");
            if (address != null) {
                // Find the TextView for delivery address in the layout
                TextView addressTextView = findViewById(R.id.deliveryAddress);
                if (addressTextView != null) {
                    addressTextView.setText(address);
                }
            }

            // Update estimated delivery time
            Long createdAt = document.getLong("createdAt");
            if (createdAt != null) {
                updateEstimatedTime(status, createdAt);
            }

            // Update delivery fee and total
            Long deliveryFee = document.getLong("deliveryFee");
            Long total = document.getLong("total");

            Log.d(TAG, "Order updated - Status: " + status + ", Total: " + total);

        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }

    private void updateOrderStatus(String status) {
        // Reset all progress indicators
        resetProgressIndicators();

        switch (status.toLowerCase()) {
            case "pending":
                updateProgressStep(1, "Order Received", "Estimated delivery in 25-30 minutes");
                statusText.setText("Order\nReceived");
                break;

            case "preparing":
                updateProgressStep(2, "Preparing", "Estimated delivery in 20-25 minutes");
                statusText.setText("Preparing");
                break;

            case "out_for_delivery":
                updateProgressStep(3, "Out for Delivery", "Estimated delivery in 10-15 minutes");
                statusText.setText("Out for\nDelivery");
                break;

            case "delivered":
                updateProgressStep(4, "Delivered", "Order completed");
                statusText.setText("Delivered");
                break;

            default:
                updateProgressStep(1, "Order Received", "Processing your order");
                statusText.setText("Processing");
                break;
        }
    }

    private void resetProgressIndicators() {
        // Reset all dots to gray
        if (orderReceivedDot != null) orderReceivedDot.setBackgroundResource(R.drawable.circle_gray);
        if (preparingDot != null) preparingDot.setBackgroundResource(R.drawable.circle_gray);
        if (outForDeliveryDot != null) outForDeliveryDot.setBackgroundResource(R.drawable.circle_gray);
        if (deliveredDot != null) deliveredDot.setBackgroundResource(R.drawable.circle_gray);

        // Reset all progress bars to gray
        if (progressBar1 != null) progressBar1.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        if (progressBar2 != null) progressBar2.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        if (progressBar3 != null) progressBar3.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        // Reset text colors
        if (orderReceivedText != null) orderReceivedText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        if (preparingText != null) preparingText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        if (outForDeliveryText != null) outForDeliveryText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        if (deliveredText != null) deliveredText.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void updateProgressStep(int step, String currentStepName, String timeText) {
        int greenColor = getResources().getColor(android.R.color.holo_green_dark);
        int redColor = getResources().getColor(android.R.color.holo_red_dark);

        estimatedTime.setText(timeText);

        // Update completed steps (green)
        if (step >= 1) {
            if (orderReceivedDot != null) orderReceivedDot.setBackgroundResource(R.drawable.circle_green);
            if (orderReceivedText != null) orderReceivedText.setTextColor(greenColor);
        }

        if (step >= 2) {
            if (preparingDot != null) preparingDot.setBackgroundResource(R.drawable.circle_green);
            if (preparingText != null) preparingText.setTextColor(greenColor);
            if (progressBar1 != null) progressBar1.setBackgroundColor(greenColor);
        }

        if (step >= 3) {
            if (outForDeliveryDot != null) outForDeliveryDot.setBackgroundResource(R.drawable.circle_green);
            if (outForDeliveryText != null) outForDeliveryText.setTextColor(greenColor);
            if (progressBar2 != null) progressBar2.setBackgroundColor(greenColor);
        }

        if (step >= 4) {
            if (deliveredDot != null) deliveredDot.setBackgroundResource(R.drawable.circle_green);
            if (deliveredText != null) deliveredText.setTextColor(greenColor);
            if (progressBar3 != null) progressBar3.setBackgroundColor(greenColor);
        }

        // Highlight current step (red) if not completed
        if (step < 4) {
            switch (step) {
                case 1:
                    if (orderReceivedDot != null) orderReceivedDot.setBackgroundResource(R.drawable.circle_red);
                    if (orderReceivedText != null) orderReceivedText.setTextColor(redColor);
                    break;
                case 2:
                    if (preparingDot != null) preparingDot.setBackgroundResource(R.drawable.circle_red);
                    if (preparingText != null) preparingText.setTextColor(redColor);
                    break;
                case 3:
                    if (outForDeliveryDot != null) outForDeliveryDot.setBackgroundResource(R.drawable.circle_red);
                    if (outForDeliveryText != null) outForDeliveryText.setTextColor(redColor);
                    break;
            }
        }
    }

    private void updateEstimatedTime(String status, long createdAt) {
        Date orderTime = new Date(createdAt);
        Date currentTime = new Date();
        long timeDiff = currentTime.getTime() - orderTime.getTime();
        int minutesPassed = (int) (timeDiff / (1000 * 60));

        String timeText;
        switch (status.toLowerCase()) {
            case "pending":
                timeText = "Estimated delivery in " + Math.max(25 - minutesPassed, 5) + "-30 minutes";
                break;
            case "preparing":
                timeText = "Estimated delivery in " + Math.max(20 - minutesPassed, 5) + "-25 minutes";
                break;
            case "out_for_delivery":
                timeText = "Estimated delivery in " + Math.max(15 - minutesPassed, 2) + " minutes";
                break;
            case "delivered":
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText = "Delivered at " + sdf.format(currentTime);
                break;
            default:
                timeText = "Processing your order";
                break;
        }

        estimatedTime.setText(timeText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when activity is destroyed
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}