package com.sithum.pizzaapp.activities.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.TicketAdapter;
import com.sithum.pizzaapp.models.SupportTicket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelpManageActivity extends AppCompatActivity implements TicketAdapter.OnTicketClickListener {

    // UI Components
    private ImageView btnBack;
    private TextView ticketCount;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private RecyclerView recyclerViewTickets;

    // Adapter and Data
    private TicketAdapter ticketAdapter;
    private List<SupportTicket> ticketList;

    // Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_manage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        loadSupportTickets();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        ticketCount = findViewById(R.id.ticketCount);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        recyclerViewTickets = findViewById(R.id.recyclerViewTickets);

        // Setup back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList, this);

        recyclerViewTickets.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTickets.setAdapter(ticketAdapter);

        // Add some spacing between items
        recyclerViewTickets.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 16; // 16dp spacing between items
            }
        });
    }

    private void loadSupportTickets() {
        showLoading(true);

        db.collection("support_tickets")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ticketList.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            SupportTicket ticket = document.toObject(SupportTicket.class);
                            if (ticket != null) {
                                ticket.setId(document.getId());
                                ticketList.add(ticket);
                            }
                        } catch (Exception e) {
                            // Log error but continue processing other tickets
                            e.printStackTrace();
                        }
                    }

                    updateUI();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showEmptyState();
                    Toast.makeText(this, "Failed to load tickets: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void updateUI() {
        ticketCount.setText(String.valueOf(ticketList.size()));

        if (ticketList.isEmpty()) {
            showEmptyState();
        } else {
            showTicketList();
        }
    }

    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recyclerViewTickets.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void showTicketList() {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerViewTickets.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        ticketAdapter.updateTickets(ticketList);
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            recyclerViewTickets.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTicketClick(SupportTicket ticket) {
        showTicketDetailsDialog(ticket);
    }

    private void showTicketDetailsDialog(SupportTicket ticket) {
        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_ticket_details, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Make dialog background rounded
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }

        // Initialize dialog views
        TextView tvDialogStatus = dialogView.findViewById(R.id.tvDialogStatus);
        TextView tvDialogSubject = dialogView.findViewById(R.id.tvDialogSubject);
        TextView tvDialogOrderNumber = dialogView.findViewById(R.id.tvDialogOrderNumber);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        TextView tvDialogTimestamp = dialogView.findViewById(R.id.tvDialogTimestamp);
        TextView tvDialogDevice = dialogView.findViewById(R.id.tvDialogDevice);
        TextView tvDialogAppVersion = dialogView.findViewById(R.id.tvDialogAppVersion);
        LinearLayout orderNumberSection = dialogView.findViewById(R.id.orderNumberSection);
        Button btnDeleteTicket = dialogView.findViewById(R.id.btnDeleteTicket);
        Button btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);

        // Populate dialog with ticket data
        populateTicketDialog(ticket, tvDialogStatus, tvDialogSubject, tvDialogOrderNumber,
                tvDialogMessage, tvDialogTimestamp, tvDialogDevice, tvDialogAppVersion,
                orderNumberSection);

        // Setup button listeners
        btnDeleteTicket.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(ticket);
        });

        btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void populateTicketDialog(SupportTicket ticket, TextView tvDialogStatus,
                                      TextView tvDialogSubject, TextView tvDialogOrderNumber,
                                      TextView tvDialogMessage, TextView tvDialogTimestamp,
                                      TextView tvDialogDevice, TextView tvDialogAppVersion,
                                      LinearLayout orderNumberSection) {

        // Status
        String status = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "OPEN";
        tvDialogStatus.setText(status);

        int statusBgRes;
        switch (status) {
            case "CLOSED":
                statusBgRes = R.drawable.status_closed_background;
                break;
            case "IN_PROGRESS":
                statusBgRes = R.drawable.status_in_progress_background;
                break;
            case "OPEN":
            default:
                statusBgRes = R.drawable.status_open_background;
                break;
        }
        tvDialogStatus.setBackgroundResource(statusBgRes);

        // Basic information
        tvDialogSubject.setText(ticket.getSubject() != null ? ticket.getSubject() : "No Subject");
        tvDialogMessage.setText(ticket.getMessage() != null ? ticket.getMessage() : "No Message");
        tvDialogDevice.setText(ticket.getDevice() != null ? ticket.getDevice() : "Unknown");
        tvDialogAppVersion.setText(ticket.getAppVersion() != null ? ticket.getAppVersion() : "Unknown");

        // Order number (show/hide section based on availability)
        if (ticket.hasOrderNumber()) {
            orderNumberSection.setVisibility(View.VISIBLE);
            tvDialogOrderNumber.setText("#" + ticket.getOrderNumber());
        } else {
            orderNumberSection.setVisibility(View.GONE);
        }

        // Format and display timestamp
        if (ticket.getTimestamp() != null) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
                tvDialogTimestamp.setText(dateFormat.format(ticket.getTimestamp().toDate()));
            } catch (Exception e) {
                tvDialogTimestamp.setText("Invalid Date");
            }
        } else {
            tvDialogTimestamp.setText("Unknown");
        }
    }

    private void showDeleteConfirmationDialog(SupportTicket ticket) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Ticket")
                .setMessage("Are you sure you want to delete this support ticket?\n\nSubject: " +
                        (ticket.getSubject() != null ? ticket.getSubject() : "No Subject") +
                        "\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTicket(ticket))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_warning);

        AlertDialog deleteDialog = builder.create();

        // Style the buttons
        deleteDialog.setOnShowListener(dialog -> {
            Button deleteButton = deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null) {
                deleteButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });

        deleteDialog.show();
    }

    private void deleteTicket(SupportTicket ticket) {
        if (ticket.getId() == null || ticket.getId().isEmpty()) {
            Toast.makeText(this, "Error: Invalid ticket ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        Toast.makeText(this, "Deleting ticket...", Toast.LENGTH_SHORT).show();

        db.collection("support_tickets")
                .document(ticket.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list
                    int position = findTicketPosition(ticket.getId());
                    if (position != -1) {
                        ticketList.remove(position);
                        ticketAdapter.removeTicket(position);
                        updateTicketCount();

                        // Check if list is now empty
                        if (ticketList.isEmpty()) {
                            showEmptyState();
                        }
                    }

                    Toast.makeText(HelpManageActivity.this,
                            "Ticket deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HelpManageActivity.this,
                            "Failed to delete ticket: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private int findTicketPosition(String ticketId) {
        for (int i = 0; i < ticketList.size(); i++) {
            if (ticketList.get(i).getId() != null && ticketList.get(i).getId().equals(ticketId)) {
                return i;
            }
        }
        return -1;
    }

    private void updateTicketCount() {
        ticketCount.setText(String.valueOf(ticketList.size()));
    }

    // Method to refresh tickets (can be called from other parts of the app)
    public void refreshTickets() {
        loadSupportTickets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tickets when returning to activity
        loadSupportTickets();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}