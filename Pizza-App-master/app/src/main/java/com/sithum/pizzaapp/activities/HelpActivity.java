package com.sithum.pizzaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sithum.pizzaapp.R;

import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    // UI Components
    private ImageView backButton;
    private TextView faqTab, contactTab;
    private LinearLayout faqContent, contactContent;
    private CardView successCard;

    // FAQ Items
    private LinearLayout faqItem1, faqItem2, faqItem3, faqItem4, faqItem5, faqItem6;
    private TextView faq1Answer, faq2Answer, faq3Answer, faq4Answer, faq5Answer, faq6Answer;
    private ImageView faq1Arrow, faq2Arrow, faq3Arrow, faq4Arrow, faq5Arrow, faq6Arrow;

    // Contact Form
    private TextInputEditText subjectEditText, messageEditText, orderNumberEditText;
    private Button submitTicketButton, submitAnotherButton;

    // Firestore
    private FirebaseFirestore db;

    private boolean isContactTabSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Header
        backButton = findViewById(R.id.backButton);

        // Tabs
        faqTab = findViewById(R.id.faqTab);
        contactTab = findViewById(R.id.contactTab);

        // Content layouts
        faqContent = findViewById(R.id.faqContent);
        contactContent = findViewById(R.id.contactContent);
        successCard = findViewById(R.id.successCard);

        // FAQ Items
        faqItem1 = findViewById(R.id.faqItem1);
        faqItem2 = findViewById(R.id.faqItem2);
        faqItem3 = findViewById(R.id.faqItem3);
        faqItem4 = findViewById(R.id.faqItem4);
        faqItem5 = findViewById(R.id.faqItem5);
        faqItem6 = findViewById(R.id.faqItem6);

        // FAQ Answers
        faq1Answer = findViewById(R.id.faq1Answer);
        faq2Answer = findViewById(R.id.faq2Answer);
        faq3Answer = findViewById(R.id.faq3Answer);
        faq4Answer = findViewById(R.id.faq4Answer);
        faq5Answer = findViewById(R.id.faq5Answer);
        faq6Answer = findViewById(R.id.faq6Answer);

        // FAQ Arrows
        faq1Arrow = findViewById(R.id.faq1Arrow);
        faq2Arrow = findViewById(R.id.faq2Arrow);
        faq3Arrow = findViewById(R.id.faq3Arrow);
        faq4Arrow = findViewById(R.id.faq4Arrow);
        faq5Arrow = findViewById(R.id.faq5Arrow);
        faq6Arrow = findViewById(R.id.faq6Arrow);

        // Contact Form
        subjectEditText = findViewById(R.id.subjectEditText);
        messageEditText = findViewById(R.id.messageEditText);
        orderNumberEditText = findViewById(R.id.orderNumberEditText);
        submitTicketButton = findViewById(R.id.submitTicketButton);
        submitAnotherButton = findViewById(R.id.submitAnotherButton);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());

        // Tab switching
        faqTab.setOnClickListener(v -> switchToFAQTab());
        contactTab.setOnClickListener(v -> switchToContactTab());

        // FAQ item clicks
        faqItem1.setOnClickListener(v -> toggleFAQItem(faq1Answer, faq1Arrow));
        faqItem2.setOnClickListener(v -> toggleFAQItem(faq2Answer, faq2Arrow));
        faqItem3.setOnClickListener(v -> toggleFAQItem(faq3Answer, faq3Arrow));
        faqItem4.setOnClickListener(v -> toggleFAQItem(faq4Answer, faq4Arrow));
        faqItem5.setOnClickListener(v -> toggleFAQItem(faq5Answer, faq5Arrow));
        faqItem6.setOnClickListener(v -> toggleFAQItem(faq6Answer, faq6Arrow));

        // Contact form buttons
        submitTicketButton.setOnClickListener(v -> submitSupportTicket());
        submitAnotherButton.setOnClickListener(v -> submitAnotherTicket());
    }

    private void switchToFAQTab() {
        if (!isContactTabSelected) return;

        isContactTabSelected = false;

        // Update tab appearance
        faqTab.setBackgroundResource(R.drawable.tab_selected_background);
        faqTab.setTextColor(getResources().getColor(android.R.color.white));
        contactTab.setBackgroundResource(R.drawable.tab_unselected_background);
        contactTab.setTextColor(getResources().getColor(R.color.grey));

        // Show/hide content
        faqContent.setVisibility(View.VISIBLE);
        contactContent.setVisibility(View.GONE);
        successCard.setVisibility(View.GONE);
    }

    private void switchToContactTab() {
        if (isContactTabSelected) return;

        isContactTabSelected = true;

        // Update tab appearance
        contactTab.setBackgroundResource(R.drawable.tab_selected_background);
        contactTab.setTextColor(getResources().getColor(android.R.color.white));
        faqTab.setBackgroundResource(R.drawable.tab_unselected_background);
        faqTab.setTextColor(getResources().getColor(R.color.grey));

        // Show/hide content
        faqContent.setVisibility(View.GONE);
        contactContent.setVisibility(View.VISIBLE);
        successCard.setVisibility(View.GONE);
    }

    private void toggleFAQItem(TextView answerTextView, ImageView arrowImageView) {
        boolean isVisible = answerTextView.getVisibility() == View.VISIBLE;

        if (isVisible) {
            // Hide answer
            answerTextView.setVisibility(View.GONE);
            rotateArrow(arrowImageView, 180f, 0f);
        } else {
            // Show answer
            answerTextView.setVisibility(View.VISIBLE);
            rotateArrow(arrowImageView, 0f, 180f);
        }
    }

    private void rotateArrow(ImageView arrow, float fromDegree, float toDegree) {
        RotateAnimation rotate = new RotateAnimation(
                fromDegree, toDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.startAnimation(rotate);
    }

    private void submitSupportTicket() {
        String subject = subjectEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        String orderNumber = orderNumberEditText.getText().toString().trim();

        // Validate input
        if (subject.isEmpty()) {
            subjectEditText.setError("Subject is required");
            subjectEditText.requestFocus();
            return;
        }

        if (message.isEmpty()) {
            messageEditText.setError("Message is required");
            messageEditText.requestFocus();
            return;
        }

        // Show loading state
        submitTicketButton.setText("Submitting...");
        submitTicketButton.setEnabled(false);

        // Save to Firestore
        saveSupportTicketToFirestore(subject, message, orderNumber);
    }

    private void saveSupportTicketToFirestore(String subject, String message, String orderNumber) {
        // Create a new support ticket document
        Map<String, Object> supportTicket = new HashMap<>();
        supportTicket.put("subject", subject);
        supportTicket.put("message", message);
        supportTicket.put("orderNumber", orderNumber.isEmpty() ? null : orderNumber);
        supportTicket.put("timestamp", com.google.firebase.Timestamp.now());
        supportTicket.put("status", "open");
        supportTicket.put("device", "Android");
        supportTicket.put("appVersion", "Pizza App");

        // Add the document to the "support_tickets" collection
        db.collection("support_tickets")
                .add(supportTicket)
                .addOnSuccessListener(documentReference -> {
                    // Success
                    showSuccessMessage();
                    Toast.makeText(HelpActivity.this, "Support ticket submitted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error
                    Toast.makeText(HelpActivity.this, "Failed to submit ticket: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Reset button state
                    submitTicketButton.setText("Submit Ticket");
                    submitTicketButton.setEnabled(true);
                });
    }

    private void showSuccessMessage() {
        // Reset button state
        submitTicketButton.setText("Submit Ticket");
        submitTicketButton.setEnabled(true);

        // Clear form
        subjectEditText.setText("");
        messageEditText.setText("");
        orderNumberEditText.setText("");

        // Hide contact form and show success message
        contactContent.setVisibility(View.GONE);
        successCard.setVisibility(View.VISIBLE);
    }

    private void submitAnotherTicket() {
        // Show contact form again and hide success message
        successCard.setVisibility(View.GONE);
        contactContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (successCard.getVisibility() == View.VISIBLE) {
            // If success card is showing, go back to contact form
            submitAnotherTicket();
        } else if (isContactTabSelected) {
            // If contact tab is selected, switch to FAQ tab
            switchToFAQTab();
        } else {
            // Otherwise, close the activity
            super.onBackPressed();
        }
    }
}