package com.sithum.pizzaapp.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.sithum.pizzaapp.adapters.OrderAdapter;
import com.sithum.pizzaapp.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderManageActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private static final String TAG = "OrderManageActivity";

    // UI Components
    private ImageView backButton;
    private TextView tabAll, tabPending, tabPreparing, tabInTransit, tabDelivered;
    private RecyclerView ordersRecyclerView;

    // Order Detail Modal Components
    private FrameLayout orderDetailOverlay;
    private ImageView closeButton;
    private TextView orderIdText, orderTimeText, orderStatusBadge;
    private TextView customerNameText, customerAddressText, customerPhoneText;
    private TextView totalAmountText, branchText, paymentMethodText, driverText, deliveryStatusText;
    private LinearLayout orderItemsContainer;
    private LinearLayout actionButtonsContainer, secondaryActionsLayout, driverAssignmentSection, locationTrackingSection;
    private Button primaryActionButton, startPreparingButton, cancelOrderButton;
    private Button markDeliveredButton, sendForDeliveryButton, assignDriverButton, cancelAssignmentButton;
    private Spinner driverSpinner;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Data
    private List<Order> ordersList = new ArrayList<>();
    private List<Map<String, String>> driversList = new ArrayList<>();
    private OrderAdapter orderAdapter;
    private String currentFilter = "all";
    private Order selectedOrder;
    private String currentUserBranchId; // User's assigned branch ID
    private boolean hasAllBranchesAccess = false; // Whether user can see all branches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_manage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Load user's branch info first, then drivers and orders
        loadCurrentUserBranch();
    }

    private void initViews() {
        // Main UI
        backButton = findViewById(R.id.backButton);
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabPreparing = findViewById(R.id.tabPreparing);
        tabInTransit = findViewById(R.id.tabInTransit);
        tabDelivered = findViewById(R.id.tabDelivered);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        // Order Detail Modal
        orderDetailOverlay = findViewById(R.id.orderDetailOverlay);
        closeButton = findViewById(R.id.closeButton);
        orderIdText = findViewById(R.id.orderIdText);
        orderTimeText = findViewById(R.id.orderTimeText);
        orderStatusBadge = findViewById(R.id.orderStatusBadge);
        customerNameText = findViewById(R.id.customerNameText);
        customerAddressText = findViewById(R.id.customerAddressText);
        customerPhoneText = findViewById(R.id.customerPhoneText);
        totalAmountText = findViewById(R.id.totalAmountText);
        branchText = findViewById(R.id.branchText);
        paymentMethodText = findViewById(R.id.paymentMethodText);
        driverText = findViewById(R.id.driverText);
        deliveryStatusText = findViewById(R.id.deliveryStatusText);
        orderItemsContainer = findViewById(R.id.orderItemsContainer);
        locationTrackingSection = findViewById(R.id.locationTrackingSection);

        // Action buttons and sections
        actionButtonsContainer = findViewById(R.id.actionButtonsContainer);
        secondaryActionsLayout = findViewById(R.id.secondaryActionsLayout);
        driverAssignmentSection = findViewById(R.id.driverAssignmentSection);
        primaryActionButton = findViewById(R.id.primaryActionButton);
        startPreparingButton = findViewById(R.id.startPreparingButton);
        cancelOrderButton = findViewById(R.id.cancelOrderButton);
        markDeliveredButton = findViewById(R.id.markDeliveredButton);
        sendForDeliveryButton = findViewById(R.id.sendForDeliveryButton);
        assignDriverButton = findViewById(R.id.assignDriverButton);
        cancelAssignmentButton = findViewById(R.id.cancelAssignmentButton);
        driverSpinner = findViewById(R.id.driverSpinner);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(ordersList, this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        closeButton.setOnClickListener(v -> hideOrderDetail());

        // Tab click listeners
        tabAll.setOnClickListener(v -> {
            updateTabSelection("all");
            loadOrders("all");
        });

        tabPending.setOnClickListener(v -> {
            updateTabSelection("pending");
            loadOrders("pending");
        });

        tabPreparing.setOnClickListener(v -> {
            updateTabSelection("preparing");
            loadOrders("preparing");
        });

        tabInTransit.setOnClickListener(v -> {
            updateTabSelection("out_for_delivery");
            loadOrders("out_for_delivery");
        });

        tabDelivered.setOnClickListener(v -> {
            updateTabSelection("delivered");
            loadOrders("delivered");
        });

        // Action button listeners
        primaryActionButton.setOnClickListener(v -> hideOrderDetail());

        startPreparingButton.setOnClickListener(v -> updateOrderStatus("preparing"));
        cancelOrderButton.setOnClickListener(v -> updateOrderStatus("cancelled"));
        sendForDeliveryButton.setOnClickListener(v -> sendOrderForDelivery());
        markDeliveredButton.setOnClickListener(v -> updateOrderStatus("delivered"));

        assignDriverButton.setOnClickListener(v -> assignDriverToOrder());
        cancelAssignmentButton.setOnClickListener(v -> hideDriverAssignment());
    }

    private void loadCurrentUserBranch() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserBranchId = documentSnapshot.getString("branch");

                        // Check if user has access to all branches (no branchId or branchId is null/empty)
                        hasAllBranchesAccess = (currentUserBranchId == null || currentUserBranchId.isEmpty());

                        Log.d(TAG, "User branch: " + currentUserBranchId + ", Has all access: " + hasAllBranchesAccess);

                        // Now load drivers and orders
                        loadDrivers();
                        loadOrders("all");
                    } else {
                        Toast.makeText(this, "Unable to load user information", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user information", e);
                    Toast.makeText(this, "Failed to load user information", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadDrivers() {
        Query driversQuery = db.collection("users")
                .whereEqualTo("role", "driver");

        // If user doesn't have all branches access, filter drivers by branch
        if (!hasAllBranchesAccess && currentUserBranchId != null) {
            driversQuery = driversQuery.whereEqualTo("branch", currentUserBranchId);
        }

        driversQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    driversList.clear();

                    // Add default option
                    Map<String, String> defaultOption = new HashMap<>();
                    defaultOption.put("id", "");
                    defaultOption.put("name", "Select a driver");
                    driversList.add(defaultOption);

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Map<String, String> driver = new HashMap<>();
                        driver.put("id", document.getId());
                        driver.put("name", document.getString("fullName"));
                        driversList.add(driver);
                    }

                    setupDriverSpinner();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading drivers", e);
                    Toast.makeText(this, "Failed to load drivers", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupDriverSpinner() {
        List<String> driverNames = new ArrayList<>();
        for (Map<String, String> driver : driversList) {
            driverNames.add(driver.get("name"));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, driverNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        driverSpinner.setAdapter(adapter);
    }

    private void updateTabSelection(String selectedTab) {
        // Reset all tabs
        resetTabStyles();

        currentFilter = selectedTab;

        // Highlight selected tab
        switch (selectedTab) {
            case "all":
                highlightTab(tabAll);
                break;
            case "pending":
                highlightTab(tabPending);
                break;
            case "preparing":
                highlightTab(tabPreparing);
                break;
            case "out_for_delivery":
                highlightTab(tabInTransit);
                break;
            case "delivered":
                highlightTab(tabDelivered);
                break;
        }
    }

    private void resetTabStyles() {
        tabAll.setTextColor(getResources().getColor(android.R.color.black));
        tabAll.setBackgroundResource(0);

        tabPending.setTextColor(getResources().getColor(android.R.color.black));
        tabPending.setBackgroundResource(0);

        tabPreparing.setTextColor(getResources().getColor(android.R.color.black));
        tabPreparing.setBackgroundResource(0);

        tabInTransit.setTextColor(getResources().getColor(android.R.color.black));
        tabInTransit.setBackgroundResource(0);

        tabDelivered.setTextColor(getResources().getColor(android.R.color.black));
        tabDelivered.setBackgroundResource(0);
    }

    private void highlightTab(TextView tab) {
        tab.setBackgroundResource(R.drawable.chip_bg);
        tab.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void loadOrders(String status) {
        Log.d(TAG, "Loading orders with status: " + status);

        Query query = db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // Apply branch filter if currentUserBranchId is set and not "all"
        if (currentUserBranchId != null && !currentUserBranchId.isEmpty() && !currentUserBranchId.equals("all")) {
            query = query.whereEqualTo("branchId", currentUserBranchId);
        }

        // Apply status filter if not "all"
        if (!status.equals("all")) {
            query = query.whereEqualTo("status", status);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ordersList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Order order = createOrderFromDocument(document);
                            if (order != null) {
                                ordersList.add(order); // no need for branch re-check
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order document: " + document.getId(), e);
                        }
                    }

                    orderAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + ordersList.size() + " orders for branch: " +
                            (currentUserBranchId == null || currentUserBranchId.isEmpty() || currentUserBranchId.equals("all")
                                    ? "All branches"
                                    : currentUserBranchId));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading orders", e);
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                });
    }


    private Order createOrderFromDocument(DocumentSnapshot document) {
        try {
            Order order = new Order();

            order.setOrderId(document.getString("orderId"));
            order.setUserId(document.getString("userId"));
            order.setBranchId(document.getString("branchId"));
            order.setStatus(document.getString("status"));
            order.setPaymentMethod(document.getString("paymentMethod"));
            order.setPaymentStatus(document.getString("paymentStatus"));
            order.setDeliveryAddress(document.getString("deliveryAddress"));

            // Handle numeric fields safely
            Double subtotal = document.getDouble("subtotal");
            order.setSubtotal(subtotal != null ? subtotal : 0.0);

            Double deliveryFee = document.getDouble("deliveryFee");
            order.setDeliveryFee(deliveryFee != null ? deliveryFee : 0.0);

            Double total = document.getDouble("total");
            order.setTotal(total != null ? total : 0.0);

            Long createdAt = document.getLong("createdAt");
            order.setCreatedAt(createdAt != null ? createdAt : System.currentTimeMillis());

            // Get items list
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) document.get("items");
            if (itemsList != null) {
                order.setItems(itemsList);
            }

            return order;

        } catch (Exception e) {
            Log.e(TAG, "Error creating order from document", e);
            return null;
        }
    }

    @Override
    public void onOrderClick(Order order) {
        selectedOrder = order;
        showOrderDetail(order);
    }

    private void showOrderDetail(Order order) {
        loadOrderDetails(order);
        orderDetailOverlay.setVisibility(View.VISIBLE);
    }

    private void hideOrderDetail() {
        orderDetailOverlay.setVisibility(View.GONE);
        selectedOrder = null;
    }

    private void loadOrderDetails(Order order) {
        // Set basic order info
        orderIdText.setText("Order " + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())).toUpperCase());

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, hh:mm a", Locale.getDefault());
        orderTimeText.setText(sdf.format(new Date(order.getCreatedAt())));

        setOrderStatusBadge(order.getStatus());
        totalAmountText.setText(String.format(Locale.getDefault(), "LKR %.0f", order.getTotal()));

        customerAddressText.setText(order.getDeliveryAddress());
        paymentMethodText.setText(order.getPaymentMethod());
        deliveryStatusText.setText(getStatusDisplayText(order.getStatus()));

        // Load customer details
        loadCustomerDetails(order.getUserId());

        // Load branch details
        loadBranchDetails(order.getBranchId());

        // Load order items
        loadOrderItems(order.getItems());

        // Setup UI based on order status
        setupUIForOrderStatus(order.getStatus());

        // Show location only if order is NOT cancelled
        if ("cancelled".equalsIgnoreCase(order.getStatus())) {
            locationTrackingSection.setVisibility(View.GONE);
        } else {
            locationTrackingSection.setVisibility(View.VISIBLE);
        }
    }

    private void loadCustomerDetails(String userId) {
        if (userId == null || userId.isEmpty()) {
            customerNameText.setText("Unknown Customer");
            customerPhoneText.setText("N/A");
            return;
        }

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String phone = documentSnapshot.getString("phone");

                        customerNameText.setText(fullName != null ? fullName : "Unknown Customer");
                        customerPhoneText.setText(phone != null ? phone : "N/A");
                    } else {
                        customerNameText.setText("Unknown Customer");
                        customerPhoneText.setText("N/A");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading customer details", e);
                    customerNameText.setText("Unknown Customer");
                    customerPhoneText.setText("N/A");
                });
    }

    private void loadBranchDetails(String branchId) {
        if (branchId == null || branchId.isEmpty()) {
            branchText.setText("Unknown Branch");
            return;
        }

        db.collection("branches")
                .document(branchId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String branchName = documentSnapshot.getString("name");
                        branchText.setText(branchName != null ? branchName : "Unknown Branch");
                    } else {
                        branchText.setText("Unknown Branch");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading branch details", e);
                    branchText.setText("Unknown Branch");
                });
    }

    private void loadOrderItems(List<Map<String, Object>> items) {
        orderItemsContainer.removeAllViews();

        if (items == null || items.isEmpty()) {
            return;
        }

        for (Map<String, Object> item : items) {
            addOrderItemView(item);
        }
    }

    private void addOrderItemView(Map<String, Object> item) {
        View itemView = getLayoutInflater().inflate(R.layout.item_order_detail, orderItemsContainer, false);

        TextView itemNameText = itemView.findViewById(R.id.itemNameText);
        TextView itemQuantityText = itemView.findViewById(R.id.itemQuantityText);
        TextView itemPriceText = itemView.findViewById(R.id.itemPriceText);

        String itemName = (String) item.get("productName");
        Object quantityObj = item.get("quantity");
        Object priceObj = item.get("basePrice");

        int quantity = 1;
        if (quantityObj instanceof Long) {
            quantity = ((Long) quantityObj).intValue();
        } else if (quantityObj instanceof Integer) {
            quantity = (Integer) quantityObj;
        }

        double price = 0.0;
        if (priceObj instanceof Double) {
            price = (Double) priceObj;
        } else if (priceObj instanceof Long) {
            price = ((Long) priceObj).doubleValue();
        }

        itemNameText.setText(itemName != null ? itemName : "Unknown Item");
        itemQuantityText.setText("Quantity: " + quantity);
        itemPriceText.setText(String.format(Locale.getDefault(), "LKR %.0f", price));

        orderItemsContainer.addView(itemView);

        Log.d("OrderItem", "Item: " + item.toString());
    }

    private void setupUIForOrderStatus(String status) {
        // Hide all sections first
        secondaryActionsLayout.setVisibility(View.GONE);
        driverAssignmentSection.setVisibility(View.GONE);
        markDeliveredButton.setVisibility(View.GONE);
        sendForDeliveryButton.setVisibility(View.GONE);

        switch (status.toLowerCase()) {
            case "pending":
                secondaryActionsLayout.setVisibility(View.VISIBLE);
                primaryActionButton.setText("Close");
                break;

            case "preparing":
                driverAssignmentSection.setVisibility(View.VISIBLE);
                primaryActionButton.setText("Close");
                break;

            case "out_for_delivery":
                markDeliveredButton.setVisibility(View.VISIBLE);
                primaryActionButton.setText("Close");
                driverText.setText("Assigned"); // You can load actual driver name here
                break;

            default:
                primaryActionButton.setText("Close");
                break;
        }
    }

    private void updateOrderStatus(String newStatus) {
        if (selectedOrder == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        if (newStatus.equals("delivered")) {
            updates.put("deliveredAt", System.currentTimeMillis());
        }

        db.collection("orders")
                .document(selectedOrder.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order status updated successfully", Toast.LENGTH_SHORT).show();
                    selectedOrder.setStatus(newStatus);
                    setupUIForOrderStatus(newStatus);
                    setOrderStatusBadge(newStatus);
                    deliveryStatusText.setText(getStatusDisplayText(newStatus));

                    // Refresh the orders list
                    loadOrders(currentFilter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating order status", e);
                    Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendOrderForDelivery() {
        if (selectedOrder == null) return;

        int selectedPosition = driverSpinner.getSelectedItemPosition();
        if (selectedPosition == 0 || selectedPosition == -1) {
            Toast.makeText(this, "Please select a driver", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverId = driversList.get(selectedPosition).get("id");
        String driverName = driversList.get(selectedPosition).get("name");

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "out_for_delivery");
        updates.put("driverId", driverId);
        updates.put("assignedAt", System.currentTimeMillis());

        db.collection("orders")
                .document(selectedOrder.getOrderId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order assigned to driver and sent for delivery", Toast.LENGTH_SHORT).show();
                    selectedOrder.setStatus("out_for_delivery");
                    driverText.setText(driverName);
                    setupUIForOrderStatus("out_for_delivery");
                    setOrderStatusBadge("out_for_delivery");
                    deliveryStatusText.setText(getStatusDisplayText("out_for_delivery"));

                    // Refresh the orders list
                    loadOrders(currentFilter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error assigning driver", e);
                    Toast.makeText(this, "Failed to assign driver", Toast.LENGTH_SHORT).show();
                });
    }

    private void assignDriverToOrder() {
        sendForDeliveryButton.setVisibility(View.VISIBLE);
    }

    private void hideDriverAssignment() {
        sendForDeliveryButton.setVisibility(View.GONE);
        driverSpinner.setSelection(0);
    }

    private void setOrderStatusBadge(String status) {
        String displayStatus = getStatusDisplayText(status);
        orderStatusBadge.setText(displayStatus);

        int textColor;
        switch (status.toLowerCase()) {
            case "pending":
                textColor = getResources().getColor(R.color.orange_text, null);
                break;
            case "preparing":
                textColor = getResources().getColor(R.color.blue_text, null);
                break;
            case "out_for_delivery":
                textColor = getResources().getColor(R.color.purple_text, null);
                break;
            case "delivered":
                textColor = getResources().getColor(R.color.green_text, null);
                break;
            case "cancelled":
                textColor = getResources().getColor(R.color.red_text, null);
                break;
            default:
                textColor = getResources().getColor(android.R.color.black, null);
                break;
        }

        orderStatusBadge.setTextColor(textColor);
    }

    private String getStatusDisplayText(String status) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh orders when returning to this activity
        if (currentUserBranchId != null || hasAllBranchesAccess) {
            loadOrders(currentFilter);
        }
    }

    // In your activity/fragment
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderAdapter != null) {
            orderAdapter.clearCache();
        }
    }
}