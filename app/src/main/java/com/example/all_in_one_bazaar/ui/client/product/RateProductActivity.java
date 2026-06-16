package com.example.all_in_one_bazaar.ui.client.product;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RateProductActivity extends AppCompatActivity {

    RatingBar ratingBar;
    EditText edtReview;
    CardView btnSubmit, btnBack;
    String productId, orderId; // 🛠️ orderId ઉમેર્યું

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_product);

        // Status Bar Fix
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#1C52B2"));
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0);
            }
        }

        ratingBar = findViewById(R.id.ratingBar);
        edtReview = findViewById(R.id.edtReview);
        btnSubmit = findViewById(R.id.btnSubmitReview);
        btnBack = findViewById(R.id.btnBack);

        // 🛠️ Product ID અને Order ID બંને મેળવો
        if (getIntent() != null) {
            productId = getIntent().getStringExtra("productId");
            orderId = getIntent().getStringExtra("orderId");
        }

        if (productId == null || productId.isEmpty() || orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Error: Missing Info!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> checkBeforeSubmit());
    }

    private void checkBeforeSubmit() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(productId);

        btnSubmit.setEnabled(false);

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean alreadyRated = false;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String dbUserId = ds.child("userId").getValue(String.class);
                    String dbOrderId = ds.child("orderId").getValue(String.class); // ચેક કરો

                    // જો યુઝર આઈડી અને ઓર્ડર આઈડી બંને સેમ હોય તો જ અટકાવશે
                    if (dbUserId != null && dbUserId.equals(userId) && dbOrderId != null && dbOrderId.equals(orderId)) {
                        alreadyRated = true;
                        break;
                    }
                }

                if (alreadyRated) {
                    Toast.makeText(RateProductActivity.this, "You have already rated this order! 🌟", Toast.LENGTH_LONG).show();
                    btnSubmit.setEnabled(true);
                    finish();
                } else {
                    submitReview(reviewsRef, userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnSubmit.setEnabled(true);
                Toast.makeText(RateProductActivity.this, "Database Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitReview(DatabaseReference reviewsRef, String userId) {
        float ratingValue = ratingBar.getRating();
        String reviewText = edtReview.getText().toString().trim();

        if (ratingValue == 0) {
            Toast.makeText(this, "Please give at least 1 star! ⭐", Toast.LENGTH_SHORT).show();
            btnSubmit.setEnabled(true);
            return;
        }

        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);

        HashMap<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("rating", ratingValue);
        reviewMap.put("comment", reviewText);
        reviewMap.put("userId", userId);
        reviewMap.put("orderId", orderId); // 🛠️ FIX: ઓર્ડર આઈડી ડેટાબેઝમાં સેવ થશે!

        reviewsRef.push().setValue(reviewMap).addOnCompleteListener(task -> {
            btnSubmit.setEnabled(true);
            if (task.isSuccessful()) {
                updateProductAverageRating(reviewsRef, productRef);
                Toast.makeText(this, "Thank you for your review! 🎉", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductAverageRating(DatabaseReference reviewsRef, DatabaseReference productRef) {
        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalStars = 0;
                int count = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Double rating = ds.child("rating").getValue(Double.class);
                    if (rating != null) {
                        totalStars += rating;
                        count++;
                    }
                }
                if (count > 0) {
                    double average = totalStars / count;
                    productRef.child("rating").setValue(String.format("%.1f", average));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}