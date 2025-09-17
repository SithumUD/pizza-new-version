package com.sithum.pizzaapp.activities.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.adapters.UserAdapter;
import com.sithum.pizzaapp.models.User;
import com.sithum.pizzaapp.models.Branch;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserManageActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    // UI Components
    private ImageView btnBack;
    private TextView tvTitle, tvFormTitle;
    private Button btnAddUser, btnSaveUser, btnCancel, btnEditUser, btnDeactivateUser;
    private ImageView btnCloseForm, btnCloseUserDetail;

    // Layout containers
    private LinearLayout userListView;
    private ScrollView userFormView, userDetailView;
    private FrameLayout contentContainer;

    // Filter tabs
    private TextView tabAll, tabCustomer, tabStaff;

    // Form fields
    private TextInputEditText etFullName, etEmail, etPhoneNumber;
    private AutoCompleteTextView spinnerUserType, spinnerStatus, spinnerUserBranch;
    private TextInputLayout statusDropdownLayout, branchDropdownLayout;
    private TextView tvStatusLabel, tvBranchLabel;

    // User list
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredUserList = new ArrayList<>();

    // Branch list
    private List<Branch> branchList = new ArrayList<>();
    private List<String> branchNames = new ArrayList<>();
    private Map<String, String> branchNameToIdMap = new HashMap<>(); // Maps branch name to branch ID
    private Map<String, String> branchIdToNameMap = new HashMap<>(); // Maps branch ID to branch name

    // Current selected user
    private User selectedUser;
    private String currentFilter = "all";

    // Profile image
    private ImageView ivProfileImage;
    private Uri profileImageUri;
    private boolean isImageChanged = false;

    // Detail view components
    private TextView tvUserDetailName, tvDetailEmail, tvDetailPhone, tvDetailUserType;
    private TextView tvTotalOrders, tvCustomerSince, tvDetailBranch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize UI components
        initUI();

        // Setup RecyclerView
        setupRecyclerView();

        // Load branches first, then users
        loadBranches();

        // Setup listeners
        setupListeners();
    }

    private void initUI() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        btnAddUser = findViewById(R.id.btnAddUser);

        // Layout containers
        userListView = findViewById(R.id.userListView);
        userFormView = findViewById(R.id.userFormView);
        userDetailView = findViewById(R.id.userDetailView);
        contentContainer = findViewById(R.id.contentContainer);

        // Filter tabs
        tabAll = findViewById(R.id.tabAll);
        tabCustomer = findViewById(R.id.tabCustomer);
        tabStaff = findViewById(R.id.tabStaff);

        // Form components
        tvFormTitle = findViewById(R.id.tvFormTitle);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerUserBranch = findViewById(R.id.spinnerUserBranch);
        statusDropdownLayout = findViewById(R.id.statusDropdownLayout);
        branchDropdownLayout = findViewById(R.id.branchDropdownLayout);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);

        btnCloseForm = findViewById(R.id.btnCloseForm);
        btnSaveUser = findViewById(R.id.btnSaveUser);
        btnCancel = findViewById(R.id.btnCancel);

        // Profile image
        ivProfileImage = findViewById(R.id.ivProfileImage);

        // Detail view components
        tvUserDetailName = findViewById(R.id.tvUserDetailName);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailUserType = findViewById(R.id.tvDetailUserType);
        tvDetailBranch = findViewById(R.id.tvDetailBranch);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvCustomerSince = findViewById(R.id.tvCustomerSince);
        btnCloseUserDetail = findViewById(R.id.btnCloseUserDetail);
        btnEditUser = findViewById(R.id.btnEditUser);
        btnDeactivateUser = findViewById(R.id.btnDeactivateUser);

        // Setup dropdowns
        setupDropdowns();
    }

    private void setupDropdowns() {
        // User type dropdown
        String[] userTypes = new String[]{"Customer", "Staff", "Admin", "Driver"};
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, userTypes);
        spinnerUserType.setAdapter(userTypeAdapter);

        // Status dropdown
        String[] statusOptions = new String[]{"Active", "Inactive"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statusOptions);
        spinnerStatus.setAdapter(statusAdapter);

        // User type selection listener to show/hide branch dropdown
        spinnerUserType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedUserType = userTypes[position];
            toggleBranchVisibility(selectedUserType);
        });
    }

    private void toggleBranchVisibility(String userType) {
        // Show branch dropdown only for Staff, Admin, and Driver roles
        if (userType.equalsIgnoreCase("Staff") ||
                userType.equalsIgnoreCase("Admin") ||
                userType.equalsIgnoreCase("Driver")) {
            branchDropdownLayout.setVisibility(View.VISIBLE);

        } else {
            branchDropdownLayout.setVisibility(View.GONE);

        }
    }

    private void loadBranches() {
        db.collection("branches")
                .whereEqualTo("status", "active")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        branchList.clear();
                        branchNames.clear();
                        branchNameToIdMap.clear();
                        branchIdToNameMap.clear();

                        // Add "All Branches" option for admin/manager roles
                        branchNames.add("All Branches");
                        branchNameToIdMap.put("All Branches", "all");
                        branchIdToNameMap.put("all", "All Branches");

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Branch branch = new Branch();
                            branch.setId(document.getId());
                            branch.setName(document.getString("name"));
                            branch.setAddress(document.getString("address"));
                            branch.setManager(document.getString("manager"));
                            branch.setPhone(document.getString("phone"));
                            branch.setStatus(document.getString("status"));

                            branchList.add(branch);
                            branchNames.add(branch.getName());

                            // Create mappings between branch name and ID
                            branchNameToIdMap.put(branch.getName(), branch.getId());
                            branchIdToNameMap.put(branch.getId(), branch.getName());
                        }

                        // Setup branch dropdown adapter
                        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(
                                UserManageActivity.this,
                                android.R.layout.simple_dropdown_item_1line,
                                branchNames
                        );
                        spinnerUserBranch.setAdapter(branchAdapter);

                        // Load users after branches are loaded
                        loadUsers();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserManageActivity.this,
                                "Error loading branches: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // Still load users even if branches fail to load
                        loadUsers();
                    }
                });
    }

    private void setupRecyclerView() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(filteredUserList, this);
        recyclerViewUsers.setAdapter(userAdapter);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add user button
        btnAddUser.setOnClickListener(v -> showAddUserForm());

        // Filter tabs
        tabAll.setOnClickListener(v -> filterUsers("all"));
        tabCustomer.setOnClickListener(v -> filterUsers("customer"));
        tabStaff.setOnClickListener(v -> filterUsers("staff"));

        // Form buttons
        btnCloseForm.setOnClickListener(v -> showUserList());
        btnCancel.setOnClickListener(v -> showUserList());
        btnSaveUser.setOnClickListener(v -> saveUser());

        // Detail view buttons
        btnCloseUserDetail.setOnClickListener(v -> showUserList());
        btnEditUser.setOnClickListener(v -> editUser());
        btnDeactivateUser.setOnClickListener(v -> deactivateUser());

        // Profile image selection
        findViewById(R.id.profileImageCard).setOnClickListener(v -> selectProfileImage());
    }

    private void loadUsers() {
        db.collection("users")
                .orderBy("fullName", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(UserManageActivity.this, "Error loading users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            User user = doc.toObject(User.class);
                            user.setId(doc.getId());
                            userList.add(user);
                        }

                        filterUsers(currentFilter);
                    }
                });
    }

    private void filterUsers(String filter) {
        currentFilter = filter;
        filteredUserList.clear();

        for (User user : userList) {
            if (filter.equals("all")) {
                filteredUserList.add(user);
            } else if (filter.equals("customer") && "customer".equalsIgnoreCase(user.getRole())) {
                filteredUserList.add(user);
            } else if (filter.equals("staff") &&
                    ("staff".equalsIgnoreCase(user.getRole())
                            || "admin".equalsIgnoreCase(user.getRole())
                            || "driver".equalsIgnoreCase(user.getRole()))) {
                filteredUserList.add(user);
            }
        }

        userAdapter.notifyDataSetChanged();
        updateTabStyles(filter);
    }

    private void updateTabStyles(String selectedTab) {
        int selectedColor = getResources().getColor(R.color.pink);
        int normalColor = getResources().getColor(android.R.color.darker_gray);

        tabAll.setTextColor(selectedTab.equals("all") ? selectedColor : normalColor);
        tabCustomer.setTextColor(selectedTab.equals("customer") ? selectedColor : normalColor);
        tabStaff.setTextColor(selectedTab.equals("staff") ? selectedColor : normalColor);

        tabAll.setBackgroundResource(selectedTab.equals("all") ? R.drawable.chip_bg : android.R.color.transparent);
        tabCustomer.setBackgroundResource(selectedTab.equals("customer") ? R.drawable.chip_bg : android.R.color.transparent);
        tabStaff.setBackgroundResource(selectedTab.equals("staff") ? R.drawable.chip_bg : android.R.color.transparent);
    }

    private void showAddUserForm() {
        userListView.setVisibility(View.GONE);
        userFormView.setVisibility(View.VISIBLE);
        userDetailView.setVisibility(View.GONE);

        tvFormTitle.setText("Add New User");
        clearForm();

        // Hide status dropdown for new users
        statusDropdownLayout.setVisibility(View.GONE);
        tvStatusLabel.setVisibility(View.GONE);

        // Show/hide branch dropdown based on default user type
        toggleBranchVisibility("Customer");
    }

    private void showEditUserForm(User user) {
        userListView.setVisibility(View.GONE);
        userFormView.setVisibility(View.VISIBLE);
        userDetailView.setVisibility(View.GONE);

        tvFormTitle.setText("Edit User");
        selectedUser = user;

        // Fill form with user data
        etFullName.setText(user.getFullName());
        etEmail.setText(user.getEmail());
        etPhoneNumber.setText(user.getPhone());
        spinnerUserType.setText(capitalizeFirst(user.getRole()), false);

        // Set branch if user has one - convert branch ID to branch name for display
        if (user.getBranch() != null && !user.getBranch().isEmpty()) {
            String branchName = branchIdToNameMap.get(user.getBranch());
            if (branchName != null) {
                spinnerUserBranch.setText(branchName, false);
            } else {
                spinnerUserBranch.setText("All Branches", false);
            }
        } else {
            spinnerUserBranch.setText("All Branches", false);
        }

        // Show status dropdown for existing users
        statusDropdownLayout.setVisibility(View.VISIBLE);
        tvStatusLabel.setVisibility(View.VISIBLE);

        // Set status
        if (user.getStatus() != null) {
            spinnerStatus.setText(capitalizeFirst(user.getStatus()), false);
        } else {
            spinnerStatus.setText("Active", false);
        }

        // Show/hide branch dropdown based on user role
        toggleBranchVisibility(capitalizeFirst(user.getRole()));

        // Load profile image if exists
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(user.getProfileImageUrl()).into(ivProfileImage);
            ivProfileImage.setVisibility(View.VISIBLE);
        } else {
            ivProfileImage.setVisibility(View.GONE);
        }
    }

    private void showUserDetail(User user) {
        userListView.setVisibility(View.GONE);
        userFormView.setVisibility(View.GONE);
        userDetailView.setVisibility(View.VISIBLE);

        selectedUser = user;

        // Fill detail view with user data
        tvUserDetailName.setText(user.getFullName());
        tvDetailEmail.setText(user.getEmail());
        tvDetailPhone.setText(user.getPhone());
        tvDetailUserType.setText(capitalizeFirst(user.getRole()));

        // Show branch information if user has one - convert branch ID to branch name for display
        if (user.getBranch() != null && !user.getBranch().isEmpty()) {
            String branchName = branchIdToNameMap.get(user.getBranch());
            if (branchName != null) {
                tvDetailBranch.setText("Branch: " + branchName);
                tvDetailBranch.setVisibility(View.VISIBLE);
            } else {
                tvDetailBranch.setVisibility(View.GONE);
            }
        } else {
            tvDetailBranch.setVisibility(View.GONE);
        }

        // For customers, show additional info
        if ("customer".equalsIgnoreCase(user.getRole())) {
            findViewById(R.id.customerInfoCard).setVisibility(View.VISIBLE);
            // You would fetch order data from Firestore here
            tvTotalOrders.setText("12"); // Example data
            tvCustomerSince.setText("May 2023"); // Example data
        } else {
            findViewById(R.id.customerInfoCard).setVisibility(View.GONE);
        }
    }

    private void showUserList() {
        userListView.setVisibility(View.VISIBLE);
        userFormView.setVisibility(View.GONE);
        userDetailView.setVisibility(View.GONE);
        selectedUser = null;
        profileImageUri = null;
        isImageChanged = false;
    }

    private void clearForm() {
        etFullName.setText("");
        etEmail.setText("");
        etPhoneNumber.setText("");
        spinnerUserType.setText("Customer", false);
        spinnerStatus.setText("Active", false);
        spinnerUserBranch.setText("All Branches", false);
        ivProfileImage.setVisibility(View.GONE);
        profileImageUri = null;
        isImageChanged = false;
    }

    private void saveUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String userType = spinnerUserType.getText().toString();
        String status = spinnerStatus.getText().toString();
        String branchName = spinnerUserBranch.getText().toString();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }

        // Validate branch selection for staff roles
        if ((userType.equalsIgnoreCase("Staff") ||
                userType.equalsIgnoreCase("Admin") ||
                userType.equalsIgnoreCase("Driver")) &&
                TextUtils.isEmpty(branchName)) {
            Toast.makeText(this, "Please select a branch for " + userType + " role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert branch name to branch ID
        String branchId = null;
        if (!userType.equalsIgnoreCase("Customer") && !TextUtils.isEmpty(branchName)) {
            branchId = branchNameToIdMap.get(branchName);
        }

        if (selectedUser == null) {
            // Create new user
            createNewUser(fullName, email, phone, userType, branchId);
        } else {
            // Update existing user
            updateUser(fullName, email, phone, userType, status, branchId);
        }
    }

    private void createNewUser(String fullName, String email, String phone, String userType, String branchId) {
        // First create auth user
        mAuth.createUserWithEmailAndPassword(email, "defaultPassword123")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser != null ? firebaseUser.getUid() : UUID.randomUUID().toString();

                            // Create user object
                            User user = new User();
                            user.setId(userId);
                            user.setFullName(fullName);
                            user.setEmail(email);
                            user.setPhone(phone);
                            user.setRole(userType.toLowerCase());
                            user.setCreatedAt(System.currentTimeMillis());
                            user.setStatus("active");

                            // Set branch ID for non-customer roles
                            if (!userType.equalsIgnoreCase("Customer") && branchId != null) {
                                user.setBranch(branchId);
                            }

                            // Upload profile image if selected
                            if (profileImageUri != null) {
                                uploadProfileImage(user);
                            } else {
                                saveUserToFirestore(user);
                            }
                        } else {
                            Toast.makeText(UserManageActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUser(String fullName, String email, String phone, String userType, String status, String branchId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("role", userType.toLowerCase());
        updates.put("status", status.toLowerCase());
        updates.put("updatedAt", System.currentTimeMillis());

        // Add branch ID for non-customer roles, remove for customer
        if (userType.equalsIgnoreCase("Customer")) {
            updates.put("branch", null);
        } else if (branchId != null) {
            updates.put("branch", branchId);
        }

        if (isImageChanged && profileImageUri != null) {
            // Update user object for image upload
            selectedUser.setFullName(fullName);
            selectedUser.setEmail(email);
            selectedUser.setPhone(phone);
            selectedUser.setRole(userType.toLowerCase());
            selectedUser.setStatus(status.toLowerCase());
            selectedUser.setBranch(userType.equalsIgnoreCase("Customer") ? null : branchId);
            selectedUser.setUpdatedAt(System.currentTimeMillis());

            uploadProfileImage(selectedUser);
        } else {
            db.collection("users").document(selectedUser.getId())
                    .update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(UserManageActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                            showUserList();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserManageActivity.this, "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadProfileImage(User user) {
        StorageReference fileRef = storageRef.child(user.getId() + ".jpg");
        fileRef.putFile(profileImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                user.setProfileImageUrl(uri.toString());
                                saveUserToFirestore(user);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserManageActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", user.getFullName());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole());
        userData.put("status", user.getStatus() != null ? user.getStatus() : "active");
        userData.put("createdAt", user.getCreatedAt());

        if (user.getProfileImageUrl() != null) {
            userData.put("profileImageUrl", user.getProfileImageUrl());
        }

        // Add branch ID for non-customer roles
        if (user.getBranch() != null && !user.getBranch().isEmpty()) {
            userData.put("branch", user.getBranch()); // This is now the branch ID
        }

        if (user.getUpdatedAt() != null) {
            userData.put("updatedAt", user.getUpdatedAt());
        }

        db.collection("users").document(user.getId())
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String action = selectedUser == null ? "created" : "updated";
                        Toast.makeText(UserManageActivity.this, "User " + action + " successfully", Toast.LENGTH_SHORT).show();
                        showUserList();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UserManageActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void editUser() {
        showEditUserForm(selectedUser);
    }

    private void deactivateUser() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate User")
                .setMessage("Are you sure you want to deactivate this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("users").document(selectedUser.getId())
                            .update("status", "inactive", "updatedAt", System.currentTimeMillis())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(UserManageActivity.this, "User deactivated", Toast.LENGTH_SHORT).show();
                                    showUserList();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UserManageActivity.this, "Error deactivating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void selectProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            Picasso.get().load(profileImageUri).into(ivProfileImage);
            ivProfileImage.setVisibility(View.VISIBLE);
            isImageChanged = true;
        }
    }

    @Override
    public void onUserClick(User user) {
        showUserDetail(user);
    }

    @Override
    public void onUserLongClick(User user) {
        // Optionally implement long click actions
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}