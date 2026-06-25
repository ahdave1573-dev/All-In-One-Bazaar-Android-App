package com.example.all_in_one_bazaar.ui.admin.order;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.adapter.AdminOrderAdapter;
import com.example.all_in_one_bazaar.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerAdminOrders;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;
    private DatabaseReference ordersRef;
    private CardView btnBack;

    // ક્લાયન્ટ એપ મુજબ ફિક્સ ડિલિવરી ચાર્જ (₹49)
    private static final double DELIVERY_CHARGE = 49.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        // 🛠️ સ્ટેટસ બારનો કલર અને સિસ્ટમ આઇકોન્સ સિંક
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#E3F2FD"));

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0);
            }
        }

        btnBack = findViewById(R.id.btnBack);
        recyclerAdminOrders = findViewById(R.id.recyclerAdminOrders);
        recyclerAdminOrders.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        adapter = new AdminOrderAdapter(this, orderList);
        recyclerAdminOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadOrders();
    }

    private void loadOrders() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Order order = new Order();

                        String orderId = ds.child("orderId").getValue(String.class);
                        order.setOrderId(orderId != null ? orderId : ds.getKey());

                        String userId = ds.child("userId").getValue(String.class);
                        order.setUserId(userId != null ? userId : "N/A");

                        // પ્રોડક્ટ પ્રાઇસ + ડિલિવરી ચાર્જ ગણતરી લોજિક
                        Object totalAmtObj = ds.child("totalAmount").getValue();
                        if (totalAmtObj != null) {
                            try {
                                double productAmount = Double.parseDouble(String.valueOf(totalAmtObj));
                                double finalTotalAmount = productAmount + DELIVERY_CHARGE;
                                order.setTotalAmount(String.format(Locale.getDefault(), "%.2f", finalTotalAmount));
                            } catch (Exception e) {
                                order.setTotalAmount(String.valueOf(totalAmtObj));
                            }
                        } else {
                            order.setTotalAmount("0");
                        }

                        String status = ds.child("status").getValue(String.class);
                        order.setStatus(status != null ? status : "Pending");

                        // 🔥 FIX: લાલ લાઇન વાળી એરર અહીં સેટર મેથડ (setProductId) વાપરીને સોલ્વ કરી દીધી છે
                        String productId = ds.child("productId").getValue(String.class);
                        order.setProductId(productId != null ? productId : "");

                        Object qtyObj = ds.child("quantity").getValue();
                        int qty = 1;
                        if (qtyObj != null) {
                            try { qty = Integer.parseInt(String.valueOf(qtyObj)); } catch (Exception ignored) {}
                        }
                        order.setQuantity(qty);

                        orderList.add(order);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrdersActivity.this, "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}