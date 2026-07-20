package com.example.all_in_one_bazaar.ui.admin.reports;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SalesReportsActivity extends AppCompatActivity {

    private CardView btnBack; // અહિયાં CardView કરેલ છે
    private TextView txtTotalProducts, txtTotalOrders, txtTotalUsers;
    private TextView txtTotalRevenue, txtPendingOrders, txtCompletedOrders, txtCancelledOrders;
    private TextView txtOutOfStock, txtLowStock;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_reports);

        // Init views
        btnBack = findViewById(R.id.btnBack);
        txtTotalProducts = findViewById(R.id.txtTotalProducts);
        txtTotalOrders = findViewById(R.id.txtTotalOrders);
        txtTotalUsers = findViewById(R.id.txtTotalUsers);
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);
        txtPendingOrders = findViewById(R.id.txtPendingOrders);
        txtCompletedOrders = findViewById(R.id.txtCompletedOrders);
        txtCancelledOrders = findViewById(R.id.txtCancelledOrders);
        txtOutOfStock = findViewById(R.id.txtOutOfStock);
        txtLowStock = findViewById(R.id.txtLowStock);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        // Load all stats
        loadProductStats();
        loadOrderStats();
        loadUserStats();
    }

    private void loadProductStats() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalProducts = snapshot.getChildrenCount();
                int outOfStock = 0;
                int lowStock = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Object qtyObj = ds.child("quantity").getValue();
                        int qty = 0;
                        try { qty = Integer.parseInt(String.valueOf(qtyObj)); } catch (Exception e) { }

                        if (qty <= 0) {
                            outOfStock++;
                        } else if (qty <= 5) {
                            lowStock++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                txtTotalProducts.setText(String.valueOf(totalProducts));
                txtOutOfStock.setText(String.valueOf(outOfStock));
                txtLowStock.setText(String.valueOf(lowStock));
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SalesReportsActivity.this, "Failed to load product stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderStats() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalOrders = snapshot.getChildrenCount();
                int pending = 0;
                int completed = 0;
                int cancelled = 0;
                double totalRevenue = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        String status = ds.child("status").getValue(String.class);
                        if (status != null) {
                            switch (status) {
                                case "Pending":
                                    pending++;
                                    break;
                                case "Delivered":
                                    completed++;
                                    break;
                                case "Cancelled":
                                    cancelled++;
                                    break;
                            }
                        }

                        // Calculate revenue from non-cancelled orders
                        if (status == null || !status.equals("Cancelled")) {
                            Object amountObj = ds.child("totalAmount").getValue();
                            try {
                                totalRevenue += Double.parseDouble(String.valueOf(amountObj));
                            } catch (Exception e) { }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                txtTotalOrders.setText(String.valueOf(totalOrders));
                txtPendingOrders.setText(String.valueOf(pending));
                txtCompletedOrders.setText(String.valueOf(completed));
                txtCancelledOrders.setText(String.valueOf(cancelled));
                txtTotalRevenue.setText("₹ " + String.format("%.2f", totalRevenue));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalesReportsActivity.this, "Failed to load order stats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserStats() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUsers = snapshot.getChildrenCount();
                txtTotalUsers.setText(String.valueOf(totalUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalesReportsActivity.this, "Failed to load user stats", Toast.LENGTH_SHORT).show();
            }
        });
    }
}