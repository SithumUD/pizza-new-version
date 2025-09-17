package com.sithum.pizzaapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private static final String TAG = "OrderAdapter";
    private List<Order> orders;
    private OnOrderClickListener listener;
    private Context context;
    private FirebaseFirestore db;
    private Map<String, String> branchNameCache; // Cache for branch names

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.branchNameCache = new HashMap<>();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    // Clear cache when adapter is no longer needed
    public void clearCache() {
        branchNameCache.clear();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdText;
        private TextView orderStatusBadge;
        private TextView orderAmountText;
        private TextView customerNameText;
        private TextView orderTimeText;
        private TextView itemCountText;
        private TextView orderLocationText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderStatusBadge = itemView.findViewById(R.id.orderStatusBadge);
            orderAmountText = itemView.findViewById(R.id.orderAmountText);
            customerNameText = itemView.findViewById(R.id.customerNameText);
            orderTimeText = itemView.findViewById(R.id.orderTimeText);
            itemCountText = itemView.findViewById(R.id.itemCountText);
            orderLocationText = itemView.findViewById(R.id.orderLocationText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onOrderClick(orders.get(position));
                    }
                }
            });
        }

        public void bind(Order order) {
            // Set order ID (shortened version)
            String shortOrderId = order.getOrderId();
            if (shortOrderId.length() > 8) {
                shortOrderId = "PM-" + shortOrderId.substring(0, 4).toUpperCase();
            }
            orderIdText.setText(shortOrderId);

            // Set order amount
            orderAmountText.setText(String.format(Locale.getDefault(), "LKR %.0f", order.getTotal()));

            // Set order status with color
            setOrderStatus(order.getStatus());

            // Set order time
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            String timeText = sdf.format(new Date(order.getCreatedAt()));
            orderTimeText.setText(timeText);

            // Set item count
            int itemCount = 0;
            if (order.getItems() != null) {
                itemCount = order.getItems().size();
            }
            itemCountText.setText(itemCount + " items");

            // Set location - load branch name instead of branch ID
            loadBranchName(order.getBranchId());

            // Load customer name asynchronously
            loadCustomerName(order.getUserId());
        }

        private void setOrderStatus(String status) {
            String displayStatus;
            int textColor;
            int backgroundColor;

            switch (status.toLowerCase()) {
                case "pending":
                    displayStatus = "Pending";
                    textColor = ContextCompat.getColor(context, R.color.orange_text);
                    backgroundColor = R.drawable.status_pending_bg;
                    break;
                case "preparing":
                    displayStatus = "Preparing";
                    textColor = ContextCompat.getColor(context, R.color.blue_text);
                    backgroundColor = R.drawable.status_preparing_bg;
                    break;
                case "out_for_delivery":
                    displayStatus = "In Transit";
                    textColor = ContextCompat.getColor(context, R.color.purple_text);
                    backgroundColor = R.drawable.status_transit_bg;
                    break;
                case "delivered":
                    displayStatus = "Delivered";
                    textColor = ContextCompat.getColor(context, R.color.green_text);
                    backgroundColor = R.drawable.status_delivered_bg;
                    break;
                case "cancelled":
                    displayStatus = "Cancelled";
                    textColor = ContextCompat.getColor(context, R.color.red_text);
                    backgroundColor = R.drawable.status_cancelled_bg;
                    break;
                default:
                    displayStatus = status;
                    textColor = ContextCompat.getColor(context, android.R.color.black);
                    backgroundColor = R.drawable.status_default_bg;
                    break;
            }

            orderStatusBadge.setText(displayStatus);
            orderStatusBadge.setTextColor(textColor);
            orderStatusBadge.setBackgroundResource(backgroundColor);
        }

        private void loadCustomerName(String userId) {
            if (userId == null || userId.isEmpty()) {
                customerNameText.setText("Unknown Customer");
                return;
            }

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                customerNameText.setText(fullName);
                            } else {
                                customerNameText.setText("Unknown Customer");
                            }
                        } else {
                            customerNameText.setText("Unknown Customer");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading customer name", e);
                        customerNameText.setText("Unknown Customer");
                    });
        }

        private void loadBranchName(String branchId) {
            if (branchId == null || branchId.isEmpty()) {
                orderLocationText.setText("Unknown Branch");
                return;
            }

            // Check if branch name is already cached
            if (branchNameCache.containsKey(branchId)) {
                orderLocationText.setText(branchNameCache.get(branchId));
                return;
            }

            // Fetch branch name from Firestore
            db.collection("branches")
                    .document(branchId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String branchName = documentSnapshot.getString("name");
                            if (branchName != null && !branchName.isEmpty()) {
                                // Cache the branch name for future use
                                branchNameCache.put(branchId, branchName);
                                orderLocationText.setText(branchName);
                            } else {
                                orderLocationText.setText("Unknown Branch");
                            }
                        } else {
                            orderLocationText.setText("Unknown Branch");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading branch name", e);
                        orderLocationText.setText("Unknown Branch");
                    });
        }
    }
}