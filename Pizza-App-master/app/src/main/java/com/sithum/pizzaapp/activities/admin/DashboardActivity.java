package com.sithum.pizzaapp.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.activities.SigninActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    // UI Components
    private ImageView btnback;
    private TextView tvViewAll;

    // Statistics TextViews
    private TextView totalOrdersCount, totalOrdersChange;
    private TextView revenueAmount, revenueChange;
    private TextView activeUsersCount, activeUsersChange;
    private TextView branchesCount, branchesChange;

    // Recent Orders Views
    private CardView recentOrder1, recentOrder2;
    private TextView recentOrder1Id, recentOrder1Customer, recentOrder1Status, recentOrder1Time, recentOrder1Amount;
    private TextView recentOrder2Id, recentOrder2Customer, recentOrder2Time, recentOrder2Amount;
    private Button btnticket;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Data holders
    private int currentMonthOrders = 0;
    private int previousMonthOrders = 0;
    private double currentMonthRevenue = 0.0;
    private double previousMonthRevenue = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadDashboardData();
        setUserProfile();
    }

    private void initViews() {
        btnback = findViewById(R.id.btnBack);
        btnticket = findViewById(R.id.btnticket);
        tvViewAll = findViewById(R.id.tvViewAll);

        // Statistics views - you'll need to add IDs to your layout
        totalOrdersCount = findViewById(R.id.totalOrdersCount);
        totalOrdersChange = findViewById(R.id.totalOrdersChange);
        revenueAmount = findViewById(R.id.revenueAmount);
        revenueChange = findViewById(R.id.revenueChange);
        activeUsersCount = findViewById(R.id.activeUsersCount);
        activeUsersChange = findViewById(R.id.activeUsersChange);
        branchesCount = findViewById(R.id.branchesCount);
        branchesChange = findViewById(R.id.branchesChange);

        // Recent orders views - you'll need to add IDs to your layout
        recentOrder1 = findViewById(R.id.recentOrder1);
        recentOrder1Id = findViewById(R.id.recentOrder1Id);
        recentOrder1Customer = findViewById(R.id.recentOrder1Customer);
        recentOrder1Status = findViewById(R.id.recentOrder1Status);
        recentOrder1Time = findViewById(R.id.recentOrder1Time);
        recentOrder1Amount = findViewById(R.id.recentOrder1Amount);

        recentOrder2 = findViewById(R.id.recentOrder2);
        recentOrder2Id = findViewById(R.id.recentOrder2Id);
        recentOrder2Customer = findViewById(R.id.recentOrder2Customer);
        recentOrder2Time = findViewById(R.id.recentOrder2Time);
        recentOrder2Amount = findViewById(R.id.recentOrder2Amount);
    }

    private void setupClickListeners() {
        btnback.setOnClickListener(v -> logoutUser());
        tvViewAll.setOnClickListener(v -> navigateToOrderManagement());

        // Management cards
        CardView cardOrderManagement = findViewById(R.id.cardOrderManagement);
        CardView cardBranchManagement = findViewById(R.id.cardBranchManagement);
        CardView cardProductManagement = findViewById(R.id.cardProductManagement);
        CardView cardUserManagement = findViewById(R.id.cardUserManagement);

        btnticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, HelpManageActivity.class);
                startActivity(intent);
            }
        });

        cardOrderManagement.setOnClickListener(v -> navigateToOrderManagement());

        cardBranchManagement.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, BranchManageActivity.class);
            startActivity(intent);
        });

        cardProductManagement.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProductManageActivity.class);
            startActivity(intent);
        });

        cardUserManagement.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, UserManageActivity.class);
            startActivity(intent);
        });

        // Recent order clicks
        if (recentOrder1 != null) {
            recentOrder1.setOnClickListener(v -> navigateToOrderManagement());
        }
        if (recentOrder2 != null) {
            recentOrder2.setOnClickListener(v -> navigateToOrderManagement());
        }
    }

    private void navigateToOrderManagement() {
        Intent intent = new Intent(DashboardActivity.this, OrderManageActivity.class);
        startActivity(intent);
    }

    private void setUserProfile() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                String[] names = fullName.trim().split(" ");
                                String initials = "";
                                if (names.length >= 2) {
                                    initials = names[0].substring(0, 1).toUpperCase() +
                                            names[1].substring(0, 1).toUpperCase();
                                } else if (names.length == 1) {
                                    initials = names[0].substring(0, Math.min(2, names[0].length())).toUpperCase();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user profile", e));
        }
    }

    private void loadDashboardData() {
        loadTotalOrders();
        loadRevenue();
        loadActiveUsers();
        loadBranches();
        loadRecentOrders();
    }

    private void loadTotalOrders() {
        // Get current month range
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long currentMonthStart = cal.getTimeInMillis();

        // Get previous month range
        cal.add(Calendar.MONTH, -1);
        long previousMonthStart = cal.getTimeInMillis();

        // Load current month orders
        db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", currentMonthStart)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    currentMonthOrders = queryDocumentSnapshots.size();
                    if (totalOrdersCount != null) {
                        totalOrdersCount.setText(String.valueOf(currentMonthOrders));
                    }

                    // Load previous month for comparison
                    loadPreviousMonthOrders(previousMonthStart, currentMonthStart);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading total orders", e);
                    if (totalOrdersCount != null) {
                        totalOrdersCount.setText("0");
                    }
                });
    }

    private void loadPreviousMonthOrders(long previousMonthStart, long currentMonthStart) {
        db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", previousMonthStart)
                .whereLessThan("createdAt", currentMonthStart)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    previousMonthOrders = queryDocumentSnapshots.size();
                    updateOrdersChangePercentage();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading previous month orders", e));
    }

    private void updateOrdersChangePercentage() {
        if (totalOrdersChange != null) {
            if (previousMonthOrders == 0) {
                totalOrdersChange.setText(currentMonthOrders > 0 ? "+100%" : "0%");
                totalOrdersChange.setTextColor(getResources().getColor(R.color.green_text, null));
            } else {
                double changePercent = ((double) (currentMonthOrders - previousMonthOrders) / previousMonthOrders) * 100;
                String changeText = String.format(Locale.getDefault(), "%+.0f%%", changePercent);
                totalOrdersChange.setText(changeText);

                int color = changePercent >= 0 ?
                        getResources().getColor(R.color.green_text, null) :
                        getResources().getColor(R.color.red_text, null);
                totalOrdersChange.setTextColor(color);
            }
        }
    }

    private void loadRevenue() {
        // Get current month range
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long currentMonthStart = cal.getTimeInMillis();

        // Get previous month range
        cal.add(Calendar.MONTH, -1);
        long previousMonthStart = cal.getTimeInMillis();

        // Load current month revenue
        db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", currentMonthStart)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    currentMonthRevenue = 0.0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Double total = doc.getDouble("total");
                        if (total != null) {
                            currentMonthRevenue += total;
                        }
                    }

                    if (revenueAmount != null) {
                        revenueAmount.setText(String.format(Locale.getDefault(), "%.0f", currentMonthRevenue));
                    }

                    // Load previous month revenue
                    loadPreviousMonthRevenue(previousMonthStart, currentMonthStart);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading revenue", e);
                    if (revenueAmount != null) {
                        revenueAmount.setText("0");
                    }
                });
    }

    private void loadPreviousMonthRevenue(long previousMonthStart, long currentMonthStart) {
        db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", previousMonthStart)
                .whereLessThan("createdAt", currentMonthStart)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    previousMonthRevenue = 0.0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Double total = doc.getDouble("total");
                        if (total != null) {
                            previousMonthRevenue += total;
                        }
                    }
                    updateRevenueChangePercentage();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading previous month revenue", e));
    }

    private void updateRevenueChangePercentage() {
        if (revenueChange != null) {
            if (previousMonthRevenue == 0) {
                revenueChange.setText(currentMonthRevenue > 0 ? "+100%" : "0%");
                revenueChange.setTextColor(getResources().getColor(R.color.green_text, null));
            } else {
                double changePercent = ((currentMonthRevenue - previousMonthRevenue) / previousMonthRevenue) * 100;
                String changeText = String.format(Locale.getDefault(), "%+.0f%%", changePercent);
                revenueChange.setText(changeText);

                int color = changePercent >= 0 ?
                        getResources().getColor(R.color.green_text, null) :
                        getResources().getColor(R.color.red_text, null);
                revenueChange.setTextColor(color);
            }
        }
    }

    private void loadActiveUsers() {
        // Get users who placed orders in the last 30 days
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        long thirtyDaysAgo = cal.getTimeInMillis();

        db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", thirtyDaysAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Count unique user IDs
                    java.util.Set<String> uniqueUsers = new java.util.HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String userId = doc.getString("userId");
                        if (userId != null) {
                            uniqueUsers.add(userId);
                        }
                    }

                    if (activeUsersCount != null) {
                        activeUsersCount.setText(String.valueOf(uniqueUsers.size()));
                    }

                    // For simplicity, show a fixed percentage or calculate based on total users
                    if (activeUsersChange != null) {
                        activeUsersChange.setText("+5%");
                        activeUsersChange.setTextColor(getResources().getColor(R.color.green_text, null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading active users", e);
                    if (activeUsersCount != null) {
                        activeUsersCount.setText("0");
                    }
                });
    }

    private void loadBranches() {
        db.collection("branches")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int branchCount = queryDocumentSnapshots.size();
                    if (branchesCount != null) {
                        branchesCount.setText(String.valueOf(branchCount));
                    }

                    if (branchesChange != null) {
                        branchesChange.setText("0%");
                        branchesChange.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading branches", e);
                    if (branchesCount != null) {
                        branchesCount.setText("0");
                    }
                });
    }

    private void loadRecentOrders() {
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(2)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() >= 1) {
                        DocumentSnapshot firstOrder = queryDocumentSnapshots.getDocuments().get(0);
                        populateRecentOrder(firstOrder, true);
                    }

                    if (queryDocumentSnapshots.size() >= 2) {
                        DocumentSnapshot secondOrder = queryDocumentSnapshots.getDocuments().get(1);
                        populateRecentOrder(secondOrder, false);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading recent orders", e));
    }

    private void populateRecentOrder(DocumentSnapshot orderDoc, boolean isFirst) {
        String orderId = orderDoc.getString("orderId");
        String userId = orderDoc.getString("userId");
        String status = orderDoc.getString("status");
        Double total = orderDoc.getDouble("total");
        Long createdAt = orderDoc.getLong("createdAt");

        // Shorten order ID
        String shortOrderId = orderId != null && orderId.length() > 8 ?
                "PM-" + orderId.substring(0, 4).toUpperCase() : orderId;

        // Format time
        String timeText = "Unknown";
        if (createdAt != null) {
            long timeDiff = System.currentTimeMillis() - createdAt;
            long hours = timeDiff / (1000 * 60 * 60);
            if (hours < 1) {
                long minutes = timeDiff / (1000 * 60);
                timeText = "• " + minutes + " minutes ago";
            } else if (hours < 24) {
                timeText = "• " + hours + " hours ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                timeText = "• " + sdf.format(new Date(createdAt));
            }
        }

        // Format amount
        String amountText = total != null ?
                String.format(Locale.getDefault(), "LKR %.0f", total) : "LKR 0";

        if (isFirst) {
            if (recentOrder1Id != null) recentOrder1Id.setText(shortOrderId);
            if (recentOrder1Status != null) recentOrder1Status.setText(getStatusDisplayText(status));
            if (recentOrder1Time != null) recentOrder1Time.setText(timeText);
            if (recentOrder1Amount != null) recentOrder1Amount.setText(amountText);

            // Load customer name
            if (userId != null) {
                loadCustomerName(userId, true);
            }
        } else {
            if (recentOrder2Id != null) recentOrder2Id.setText(shortOrderId);
            if (recentOrder2Time != null) recentOrder2Time.setText(timeText);
            if (recentOrder2Amount != null) recentOrder2Amount.setText(amountText);

            // Load customer name
            if (userId != null) {
                loadCustomerName(userId, false);
            }
        }
    }

    private void loadCustomerName(String userId, boolean isFirst) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String customerName = "Unknown Customer";
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            customerName = fullName;
                        }
                    }

                    if (isFirst && recentOrder1Customer != null) {
                        recentOrder1Customer.setText(customerName);
                    } else if (!isFirst && recentOrder2Customer != null) {
                        recentOrder2Customer.setText(customerName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading customer name", e);
                    if (isFirst && recentOrder1Customer != null) {
                        recentOrder1Customer.setText("Unknown Customer");
                    } else if (!isFirst && recentOrder2Customer != null) {
                        recentOrder2Customer.setText("Unknown Customer");
                    }
                });
    }

    private String getStatusDisplayText(String status) {
        if (status == null) return "Unknown";

        switch (status.toLowerCase()) {
            case "pending":
                return "Pending";
            case "preparing":
                return "Preparing";
            case "out_for_delivery":
                return "In Transit";
            case "delivered":
                return "Delivered";
            case "cancelled":
                return "Cancelled";
            default:
                return status;
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DashboardActivity.this, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data when returning to this activity
        loadDashboardData();
    }
}