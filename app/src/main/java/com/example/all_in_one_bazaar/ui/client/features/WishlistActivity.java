package com.example.all_in_one_bazaar.ui.client.features;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.UserProductAdapter;
import com.example.all_in_one_bazaar.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    UserProductAdapter adapter;
    List<Product> wishlistList;
    ProgressBar progressBar;
    LinearLayout layoutEmptyWishlist;
    CardView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        // 🛠️ Status Bar Fix (ડાર્ક બ્લુ સ્ટેટસ બાર અને સફેદ અક્ષરો માટે)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 🎨 ડાર્ક બ્લુ કલર
            window.setStatusBarColor(android.graphics.Color.parseColor("#1C52B2"));

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White text
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.view.WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    insetsController.setSystemBarsAppearance(
                            0,
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                }
            }
        }

        // Views Find
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerWishlist);
        progressBar = findViewById(R.id.progressBarWishlist);
        layoutEmptyWishlist = findViewById(R.id.layoutEmptyWishlist);

        // Back Button Logic
        btnBack.setOnClickListener(v -> finish());

        // Grid Layout (એક લાઈનમાં 2 પ્રોડક્ટ)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        wishlistList = new ArrayList<>();

        // ડેટા લાવો
        fetchWishlistItems();
    }

    private void fetchWishlistItems() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference wishRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(userId);

        progressBar.setVisibility(View.VISIBLE);

        wishRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    if (product != null) {
                        wishlistList.add(product);
                    }
                }

                // Adapter સેટ કરો
                adapter = new UserProductAdapter(WishlistActivity.this, wishlistList);
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);

                // જો લિસ્ટ ખાલી હોય તો સુંદર મેસેજ બતાવો
                if (wishlistList.isEmpty()) {
                    layoutEmptyWishlist.setVisibility(View.VISIBLE);
                } else {
                    layoutEmptyWishlist.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WishlistActivity.this, "Error loading wishlist", Toast.LENGTH_SHORT).show();
            }
        });
    }
}