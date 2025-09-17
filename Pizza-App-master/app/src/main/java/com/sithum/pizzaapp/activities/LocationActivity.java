package com.sithum.pizzaapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.BranchLocationAdapter;
import com.sithum.pizzaapp.models.Branch;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity implements BranchLocationAdapter.OnBranchClickListener {

    // Firebase instance
    private FirebaseFirestore db;

    // UI Components
    private RecyclerView branchRecyclerView;
    private TextView tvTitle;

    // Branch list
    private BranchLocationAdapter branchAdapter;
    private List<Branch> branchList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        // Initialize Firebase instance
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        initUI();

        // Setup RecyclerView
        setupRecyclerView();

        // Load branches from Firestore
        loadBranches();
    }

    private void initUI() {
        tvTitle = findViewById(R.id.tvTitle);
        branchRecyclerView = findViewById(R.id.branchlist);
    }

    private void setupRecyclerView() {
        branchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        branchAdapter = new BranchLocationAdapter(branchList, this);
        branchRecyclerView.setAdapter(branchAdapter);
    }

    private void loadBranches() {
        // Get only active branches, ordered by name
        db.collection("branches")
                .whereEqualTo("status", "active")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(LocationActivity.this, "Error loading branches: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        branchList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Branch branch = doc.toObject(Branch.class);
                            branch.setId(doc.getId());
                            branchList.add(branch);
                        }

                        branchAdapter.notifyDataSetChanged();

                        // Show message if no branches found
                        if (branchList.isEmpty()) {
                            Toast.makeText(LocationActivity.this, "No branches available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBranchClick(Branch branch) {
        // Handle branch selection
        selectBranch(branch);
    }

    private void selectBranch(Branch branch) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("branchId", branch.getId());
        intent.putExtra("branchName", branch.getName());
        intent.putExtra("branchAddress", branch.getAddress());
        intent.putExtra("branchPhone", branch.getPhone());
        intent.putExtra("branchManager", branch.getManager());

        startActivity(intent);
        finish(); // optional: if you don’t want to come back here when pressing back

    }


    @Override
    public void onBackPressed() {
        // Return canceled result if user presses back
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}