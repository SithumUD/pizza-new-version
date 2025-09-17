package com.sithum.pizzaapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.sithum.pizzaapp.adapters.PaymentMethodAdapter;
import com.sithum.pizzaapp.models.PaymentMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentMethodsActivity extends AppCompatActivity implements PaymentMethodAdapter.OnPaymentMethodListener {

    private ImageView backButton;
    private RecyclerView paymentMethodList;
    private androidx.cardview.widget.CardView addNewCardButton;
    private ConstraintLayout addPaymentMethodLayout;
    private EditText cardNumberInput, cardholderInput, expiryInput, cvvInput;
    private CheckBox defaultCheckbox;
    private Button addCardButton, cancelButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private PaymentMethodAdapter adapter;
    private List<PaymentMethod> paymentMethods;

    private boolean isAddingCard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_methods);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeFirebase();
        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        setupTextWatchers();
        loadPaymentMethods();
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        paymentMethodList = findViewById(R.id.paymentmethodlist);
        addNewCardButton = findViewById(R.id.addNewCardButton);
        addPaymentMethodLayout = findViewById(R.id.addPaymentMethodLayout);

        cardNumberInput = findViewById(R.id.cardNumberInput);
        cardholderInput = findViewById(R.id.cardholderInput);
        expiryInput = findViewById(R.id.expiryInput);
        cvvInput = findViewById(R.id.cvvInput);
        defaultCheckbox = findViewById(R.id.defaultCheckbox);
        addCardButton = findViewById(R.id.addCardButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupRecyclerView() {
        paymentMethods = new ArrayList<>();
        adapter = new PaymentMethodAdapter(this, paymentMethods);
        adapter.setOnPaymentMethodListener(this);
        paymentMethodList.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodList.setAdapter(adapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        addNewCardButton.setOnClickListener(v -> showAddCardForm());

        addCardButton.setOnClickListener(v -> {
            if (validateCardDetails()) {
                addPaymentMethod();
            }
        });

        cancelButton.setOnClickListener(v -> hideAddCardForm());
    }

    private void setupTextWatchers() {
        // Card number formatting
        cardNumberInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length() && i < 16; i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                cardNumberInput.setText(formatted.toString());
                cardNumberInput.setSelection(formatted.length());
                isFormatting = false;
            }
        });

        // Expiry date formatting
        expiryInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("/", "");
                if (input.length() >= 2) {
                    input = input.substring(0, 2) + "/" + input.substring(2, Math.min(input.length(), 4));
                }

                expiryInput.setText(input);
                expiryInput.setSelection(input.length());
                isFormatting = false;
            }
        });
    }

    private void showAddCardForm() {
        isAddingCard = true;
        addPaymentMethodLayout.setVisibility(View.VISIBLE);
        clearForm();
    }

    private void hideAddCardForm() {
        isAddingCard = false;
        addPaymentMethodLayout.setVisibility(View.GONE);
        clearForm();
    }

    private void clearForm() {
        cardNumberInput.setText("");
        cardholderInput.setText("");
        expiryInput.setText("");
        cvvInput.setText("");
        defaultCheckbox.setChecked(false);
    }

    private boolean validateCardDetails() {
        String cardNumber = cardNumberInput.getText().toString().replaceAll("\\s", "");
        String cardholderName = cardholderInput.getText().toString().trim();
        String expiryDate = expiryInput.getText().toString().trim();
        String cvv = cvvInput.getText().toString().trim();

        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            cardNumberInput.setError("Invalid card number");
            return false;
        }

        if (cardholderName.isEmpty()) {
            cardholderInput.setError("Cardholder name is required");
            return false;
        }

        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            expiryInput.setError("Invalid expiry date (MM/YY)");
            return false;
        }

        if (cvv.length() < 3 || cvv.length() > 4) {
            cvvInput.setError("Invalid CVV");
            return false;
        }

        // Validate expiry date is in the future
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;

        if (month < 1 || month > 12) {
            expiryInput.setError("Invalid month");
            return false;
        }

        return true;
    }

    private void addPaymentMethod() {
        String cardNumber = cardNumberInput.getText().toString().replaceAll("\\s", "");
        String cardholderName = cardholderInput.getText().toString().trim();
        String expiryDate = expiryInput.getText().toString().trim();
        String cvv = cvvInput.getText().toString().trim();
        boolean isDefault = defaultCheckbox.isChecked();

        String cardType = PaymentMethod.detectCardType(cardNumber);

        PaymentMethod paymentMethod = new PaymentMethod(cardNumber, cardholderName,
                expiryDate, cvv, cardType, isDefault);

        addCardButton.setEnabled(false);
        addCardButton.setText("Adding...");

        // If setting as default, update existing default cards first
        if (isDefault) {
            updateExistingDefaultCards(() -> saveNewPaymentMethod(paymentMethod));
        } else {
            saveNewPaymentMethod(paymentMethod);
        }
    }

    private void updateExistingDefaultCards(Runnable callback) {
        db.collection("users").document(currentUserId)
                .collection("payment_methods")
                .whereEqualTo("default", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            doc.getReference().update("default", false);
                        }
                    }
                    callback.run();
                })
                .addOnFailureListener(e -> {
                    addCardButton.setEnabled(true);
                    addCardButton.setText("Add Card");
                    Toast.makeText(this, "Failed to update default cards", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewPaymentMethod(PaymentMethod paymentMethod) {
        db.collection("users").document(currentUserId)
                .collection("payment_methods")
                .add(paymentMethod)
                .addOnSuccessListener(documentReference -> {
                    paymentMethod.setId(documentReference.getId());
                    paymentMethods.add(paymentMethod);
                    adapter.notifyItemInserted(paymentMethods.size() - 1);

                    addCardButton.setEnabled(true);
                    addCardButton.setText("Add Card");
                    hideAddCardForm();
                    Toast.makeText(this, "Payment method added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    addCardButton.setEnabled(true);
                    addCardButton.setText("Add Card");
                    Toast.makeText(this, "Failed to add payment method", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPaymentMethods() {
        db.collection("users").document(currentUserId)
                .collection("payment_methods")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paymentMethods.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        PaymentMethod paymentMethod = document.toObject(PaymentMethod.class);
                        if (paymentMethod != null) {
                            paymentMethod.setId(document.getId());
                            paymentMethods.add(paymentMethod);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load payment methods", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteClick(PaymentMethod paymentMethod, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Payment Method")
                .setMessage("Are you sure you want to delete this payment method?")
                .setPositiveButton("Delete", (dialog, which) -> deletePaymentMethod(paymentMethod, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSetDefaultClick(PaymentMethod paymentMethod, int position) {
        // First update existing default cards
        updateExistingDefaultCards(() -> {
            // Then set this card as default
            Map<String, Object> updates = new HashMap<>();
            updates.put("default", true);

            db.collection("users").document(currentUserId)
                    .collection("payment_methods").document(paymentMethod.getId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Update local data
                        for (PaymentMethod pm : paymentMethods) {
                            pm.setDefault(false);
                        }
                        paymentMethod.setDefault(true);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Default payment method updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to set as default", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void deletePaymentMethod(PaymentMethod paymentMethod, int position) {
        db.collection("users").document(currentUserId)
                .collection("payment_methods").document(paymentMethod.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    adapter.removeItem(position);
                    Toast.makeText(this, "Payment method deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete payment method", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onBackPressed() {
        if (isAddingCard) {
            hideAddCardForm();
        } else {
            super.onBackPressed();
        }
    }
}