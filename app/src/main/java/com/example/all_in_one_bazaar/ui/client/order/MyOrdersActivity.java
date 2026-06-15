package com.example.all_in_one_bazaar.ui.client.order;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.OrderAdapter;
import com.example.all_in_one_bazaar.model.Order;
import com.example.all_in_one_bazaar.ui.auth.LoginActivity;
import com.example.all_in_one_bazaar.ui.client.cart_checkout.CartActivity;
import com.example.all_in_one_bazaar.ui.client.category.CategoryProductsActivity;
import com.example.all_in_one_bazaar.ui.client.features.NotificationActivity;
import com.example.all_in_one_bazaar.ui.client.profile.ProfileActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    OrderAdapter adapter;
    List<Order> orderList;
    ProgressBar progressBar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView imgMenu, imgTopProfile, imgNotification;
    private LinearLayout bottomNavHome, bottomNavCat, bottomNavCart, bottomNavOrder, bottomNavProfile;

    FirebaseAuth auth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBarOrders);

        imgMenu = findViewById(R.id.imgMenu);
        imgTopProfile = findViewById(R.id.imgTopProfile);
        imgNotification = findViewById(R.id.imgNotification);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        bottomNavHome = findViewById(R.id.bottomNavHome);
        bottomNavCat = findViewById(R.id.bottomNavCat);
        bottomNavCart = findViewById(R.id.bottomNavCart);
        bottomNavOrder = findViewById(R.id.bottomNavOrder);
        bottomNavProfile = findViewById(R.id.bottomNavProfile);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();

        adapter = new OrderAdapter(MyOrdersActivity.this, orderList);
        recyclerView.setAdapter(adapter);

        imgMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        imgTopProfile.setOnClickListener(v -> startActivity(new Intent(MyOrdersActivity.this, ProfileActivity.class)));

        if (imgNotification != null) {
            imgNotification.setOnClickListener(v -> {
                Intent intent = new Intent(MyOrdersActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        bottomNavHome.setOnClickListener(v -> {
            startActivity(new Intent(MyOrdersActivity.this, MainActivity.class));
            finish();
        });
        bottomNavCat.setOnClickListener(v -> {
            startActivity(new Intent(MyOrdersActivity.this, CategoryProductsActivity.class));
            finish();
        });
        bottomNavCart.setOnClickListener(v -> {
            startActivity(new Intent(MyOrdersActivity.this, CartActivity.class));
            finish();
        });
        bottomNavOrder.setOnClickListener(v -> Toast.makeText(this, "Already on Orders", Toast.LENGTH_SHORT).show());
        bottomNavProfile.setOnClickListener(v -> startActivity(new Intent(MyOrdersActivity.this, ProfileActivity.class)));

        if (currentUser != null) {
            loadUserProfile(currentUser.getUid());
            fetchOrders();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void fetchOrders() {
        if (auth.getCurrentUser() == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders");

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                if (!snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    if (adapter != null) adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Order order = new Order();
                        order.setOrderId(ds.getKey());

                        String uid = "";
                        if (ds.hasChild("userId")) {
                            uid = String.valueOf(ds.child("userId").getValue());
                        }
                        order.setUserId(uid);

                        if (!uid.equals(currentUserId)) continue;

                        // અહીં ડેટાબેઝમાંથી ઓરિજિનલ રકમ સીધી રીડ કરીશું
                        if (ds.hasChild("totalAmount")) {
                            order.setTotalAmount(String.valueOf(ds.child("totalAmount").getValue()));
                        }

                        if (ds.hasChild("status")) {
                            order.setStatus(String.valueOf(ds.child("status").getValue()));
                        } else {
                            order.setStatus("Pending");
                        }

                        if (ds.hasChild("date")) {
                            order.setDate(String.valueOf(ds.child("date").getValue()));
                        } else {
                            order.setDate("N/A");
                        }

                        if (ds.hasChild("productId")) {
                            order.setProductId(String.valueOf(ds.child("productId").getValue()));
                        }

                        orderList.add(order);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                progressBar.setVisibility(View.GONE);
                Collections.reverse(orderList);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadUserProfile(String uid) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    View headerView = navigationView.getHeaderView(0);
                    ImageView imgNavProfile = headerView.findViewById(R.id.imgNavProfile);
                    TextView txtNavName = headerView.findViewById(R.id.txtNavName);
                    TextView txtNavEmail = headerView.findViewById(R.id.txtNavEmail);

                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (name != null && txtNavName != null) txtNavName.setText(name);
                    if (email != null && txtNavEmail != null) txtNavEmail.setText(email);

                    String imageStr = snapshot.child("image").getValue(String.class);
                    if (imageStr != null && !imageStr.isEmpty() && !imageStr.equals("null")) {
                        try {
                            if (imgTopProfile != null) { imgTopProfile.setColorFilter(null); imgTopProfile.setPadding(0, 0, 0, 0); }
                            if (imgNavProfile != null) { imgNavProfile.setColorFilter(null); imgNavProfile.setPadding(0, 0, 0, 0); }

                            if (imageStr.contains(",")) imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                            imageStr = imageStr.trim();

                            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inJustDecodeBounds = true;
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
                            opts.inSampleSize = calculateInSampleSize(opts, 200, 200);
                            opts.inJustDecodeBounds = false;

                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);

                            if (decodedByte != null) {
                                if (imgTopProfile != null) imgTopProfile.setImageBitmap(decodedByte);
                                if (imgNavProfile != null) imgNavProfile.setImageBitmap(decodedByte);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(MyOrdersActivity.this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(MyOrdersActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_orders) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_logout) {
            auth.signOut();
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MyOrdersActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}