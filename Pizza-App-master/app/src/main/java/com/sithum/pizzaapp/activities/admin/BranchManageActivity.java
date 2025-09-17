package com.sithum.pizzaapp.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.BranchAdapter;
import com.sithum.pizzaapp.models.Branch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BranchManageActivity extends AppCompatActivity implements BranchAdapter.OnBranchClickListener {

    // Firebase instance
    private FirebaseFirestore db;

    // UI Components
    private ImageView btnBack;
    private TextView tvTitle, tvFormTitle;
    private Button btnAddBranch, btnSaveBranch, btnCancel, btnEditBranch, btnDeactivate;
    private ImageView btnCloseBranchDetail;

    // Layout containers
    private LinearLayout branchListView;
    private ScrollView branchFormView, branchDetailView;
    private FrameLayout contentContainer;

    // Form fields
    private TextInputEditText etBranchName, etAddress, etPhoneNumber, etBranchManager;

    // Branch list
    private RecyclerView recyclerViewBranches;
    private BranchAdapter branchAdapter;
    private List<Branch> branchList = new ArrayList<>();

    // Current selected branch
    private Branch selectedBranch;

    // Detail view components
    private TextView tvBranchDetailName, tvDetailAddress, tvDetailPhone, tvDetailManager, tvDetailStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_manage);

        // Initialize Firebase instance
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        initUI();

        // Setup RecyclerView
        setupRecyclerView();

        // Load branches from Firestore
        loadBranches();

        // Setup listeners
        setupListeners();
    }

    private void initUI() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        btnAddBranch = findViewById(R.id.btnAddBranch);

        // Layout containers
        branchListView = findViewById(R.id.branchListView);
        branchFormView = findViewById(R.id.branchFormView);
        branchDetailView = findViewById(R.id.branchDetailView);
        contentContainer = findViewById(R.id.contentContainer);

        // Form components
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etBranchName = findViewById(R.id.etBranchName);
        etAddress = findViewById(R.id.etAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBranchManager = findViewById(R.id.etBranchManager);
        btnSaveBranch = findViewById(R.id.btnSaveBranch);
        btnCancel = findViewById(R.id.btnCancel);

        // Detail view components
        tvBranchDetailName = findViewById(R.id.tvBranchDetailName);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailManager = findViewById(R.id.tvDetailManager);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        btnCloseBranchDetail = findViewById(R.id.btnCloseBranchDetail);
        btnEditBranch = findViewById(R.id.btnEditBranch);
        btnDeactivate = findViewById(R.id.btnDeactivate);
    }

    private void setupRecyclerView() {
        recyclerViewBranches = findViewById(R.id.recyclerViewBranches);
        recyclerViewBranches.setLayoutManager(new LinearLayoutManager(this));
        branchAdapter = new BranchAdapter(branchList, this);
        recyclerViewBranches.setAdapter(branchAdapter);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add branch button
        btnAddBranch.setOnClickListener(v -> showAddBranchForm());

        // Form buttons
        btnCancel.setOnClickListener(v -> showBranchList());
        btnSaveBranch.setOnClickListener(v -> saveBranch());

        // Detail view buttons
        btnCloseBranchDetail.setOnClickListener(v -> showBranchList());
        btnEditBranch.setOnClickListener(v -> editBranch());
        btnDeactivate.setOnClickListener(v -> toggleBranchStatus());
    }

    private void loadBranches() {
        db.collection("branches")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(BranchManageActivity.this, "Error loading branches: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        branchList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Branch branch = doc.toObject(Branch.class);
                            branch.setId(doc.getId());
                            branchList.add(branch);
                        }

                        branchAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddBranchForm() {
        branchListView.setVisibility(View.GONE);
        branchFormView.setVisibility(View.VISIBLE);
        branchDetailView.setVisibility(View.GONE);

        tvFormTitle.setText("Add New Branch");
        clearForm();
    }

    private void showEditBranchForm(Branch branch) {
        branchListView.setVisibility(View.GONE);
        branchFormView.setVisibility(View.VISIBLE);
        branchDetailView.setVisibility(View.GONE);

        tvFormTitle.setText("Edit Branch");
        selectedBranch = branch;

        // Fill form with branch data
        etBranchName.setText(branch.getName());
        etAddress.setText(branch.getAddress());
        etPhoneNumber.setText(branch.getPhone());
        etBranchManager.setText(branch.getManager());
    }

    private void showBranchDetail(Branch branch) {
        branchListView.setVisibility(View.GONE);
        branchFormView.setVisibility(View.GONE);
        branchDetailView.setVisibility(View.VISIBLE);

        selectedBranch = branch;

        // Fill detail view with branch data
        tvBranchDetailName.setText(branch.getName());
        tvDetailAddress.setText(branch.getAddress());
        tvDetailPhone.setText(branch.getPhone());
        tvDetailManager.setText(branch.getManager());

        // Set status
        if ("active".equals(branch.getStatus())) {
            tvDetailStatus.setText("Active");
            tvDetailStatus.setBackgroundResource(R.drawable.status_active_bg);
            btnDeactivate.setText("Deactivate");
        } else {
            tvDetailStatus.setText("Inactive");
            tvDetailStatus.setBackgroundResource(R.drawable.status_inactive_bg);
            btnDeactivate.setText("Activate");
        }
    }

    private void showBranchList() {
        branchListView.setVisibility(View.VISIBLE);
        branchFormView.setVisibility(View.GONE);
        branchDetailView.setVisibility(View.GONE);
        selectedBranch = null;
    }

    private void clearForm() {
        etBranchName.setText("");
        etAddress.setText("");
        etPhoneNumber.setText("");
        etBranchManager.setText("");
    }

    private void saveBranch() {
        String name = etBranchName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String manager = etBranchManager.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            etBranchName.setError("Branch name is required");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }

        if (selectedBranch == null) {
            // Create new branch
            createNewBranch(name, address, phone, manager);
        } else {
            // Update existing branch
            updateBranch(name, address, phone, manager);
        }
    }

    private void createNewBranch(String name, String address, String phone, String manager) {
        Map<String, Object> branchData = new HashMap<>();
        branchData.put("name", name);
        branchData.put("address", address);
        branchData.put("phone", phone);
        branchData.put("manager", manager);
        branchData.put("status", "active");
        branchData.put("createdAt", System.currentTimeMillis());

        db.collection("branches")
                .add(branchData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BranchManageActivity.this, "Branch created successfully", Toast.LENGTH_SHORT).show();
                    showBranchList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BranchManageActivity.this, "Error creating branch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBranch(String name, String address, String phone, String manager) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("address", address);
        updates.put("phone", phone);
        updates.put("manager", manager);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("branches").document(selectedBranch.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BranchManageActivity.this, "Branch updated successfully", Toast.LENGTH_SHORT).show();
                    showBranchList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BranchManageActivity.this, "Error updating branch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void editBranch() {
        showEditBranchForm(selectedBranch);
    }

    private void toggleBranchStatus() {
        String newStatus = "active".equals(selectedBranch.getStatus()) ? "inactive" : "active";
        String statusMessage = "active".equals(newStatus) ? "activated" : "deactivated";

        db.collection("branches").document(selectedBranch.getId())
                .update("status", newStatus, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BranchManageActivity.this, "Branch " + statusMessage, Toast.LENGTH_SHORT).show();
                    showBranchList();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BranchManageActivity.this, "Error updating branch status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBranchClick(Branch branch) {
        showBranchDetail(branch);
    }

    @Override
    public void onBranchLongClick(Branch branch) {
        // Optionally implement long click actions
    }
}