package com.example.all_in_one_bazaar.ui.client.cart_checkout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.CartAdapter;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.auth.LoginActivity;
import com.example.all_in_one_bazaar.ui.client.category.CategoryProductsActivity;
import com.example.all_in_one_bazaar.ui.client.features.NotificationActivity; // 🔔 આ નવું ઈમ્પોર્ટ કર્યું
import com.example.all_in_one_bazaar.ui.client.order.MyOrdersActivity;
import com.example.all_in_one_bazaar.ui.client.profile.ProfileActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // ── UI ────────────────────────────────────────────────────────────
    private RecyclerView  recyclerView;
    private CartAdapter   adapter;
    private List<Product> cartList;
    private TextView      txtTotalAmount;
    private TextView      txtEmptyCart;
    private CardView      btnPlaceOrder;
    private ProgressBar   progressBar;
    private DrawerLayout  drawerLayout;
    private NavigationView navigationView;
    private ImageView     imgMenu, imgTopProfile, imgNotification; // 🔔 imgNotification નવું એડ કર્યું

    // ── Bottom nav tabs ───────────────────────────────────────────────
    private android.widget.LinearLayout bottomNavHome, bottomNavCat,
            bottomNavCart, bottomNavOrder, bottomNavProfile;

    // ── State ─────────────────────────────────────────────────────────
    private double totalAmount = 0;

    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        bindViews();
        setupRecycler();
        setupListeners();
        loadCartItems();
    }

    // ── Bind views ────────────────────────────────────────────────────
    private void bindViews() {
        recyclerView    = findViewById(R.id.recyclerCart);
        txtTotalAmount  = findViewById(R.id.txtTotalAmount);
        txtEmptyCart    = findViewById(R.id.txtEmptyCart);
        btnPlaceOrder   = findViewById(R.id.btnPlaceOrder);
        progressBar     = findViewById(R.id.progressBar);
        drawerLayout    = findViewById(R.id.drawerLayout);
        navigationView  = findViewById(R.id.navigationView);
        imgMenu         = findViewById(R.id.imgMenu);
        imgTopProfile   = findViewById(R.id.imgTopProfile);
        imgNotification = findViewById(R.id.imgNotification); // 🔔 આઈકનને XML આઈડી સાથે લિંક કર્યું

        bottomNavHome    = findViewById(R.id.bottomNavHome);
        bottomNavCat     = findViewById(R.id.bottomNavCat);
        bottomNavCart    = findViewById(R.id.bottomNavCart);
        bottomNavOrder   = findViewById(R.id.bottomNavOrder);
        bottomNavProfile = findViewById(R.id.bottomNavProfile);
    }

    // ── RecyclerView ──────────────────────────────────────────────────
    private void setupRecycler() {
        cartList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🛠️ પરફોર્મન્સ ફિક્સ: એડેપ્ટર અહીં જ એકવાર સેટ કરી દેવું સારું રેહશે
        adapter = new CartAdapter(CartActivity.this, cartList);
        recyclerView.setAdapter(adapter);
    }

    // ── Listeners ─────────────────────────────────────────────────────
    private void setupListeners() {

        // Hamburger drawer
        imgMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else
                drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(this);

        // Top profile avatar
        if (imgTopProfile != null)
            imgTopProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));

        // 🔔 FIX: ટોપમાં નોટિફિકેશન આઇકન પર ક્લિક કરવાથી લિંક ઓપન થશે
        if (imgNotification != null) {
            imgNotification.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        // Checkout button
        btnPlaceOrder.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Your cart is empty!",
                        Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, CheckoutActivity.class));
            }
        });

        // Bottom navigation
        bottomNavHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        bottomNavCat.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryProductsActivity.class));
            finish();
        });
        bottomNavCart.setOnClickListener(v ->
                Toast.makeText(this, "Already on Cart",
                        Toast.LENGTH_SHORT).show());
        bottomNavOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, MyOrdersActivity.class));
            finish();
        });
        bottomNavProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    // ── Load cart from Firebase ───────────────────────────────────────
    private void loadCartItems() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Cart").child(uid);

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                totalAmount = 0;

                if (!snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    txtTotalAmount.setText("₹ 0.00");
                    txtEmptyCart.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    if (adapter != null) adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        String name  = valueOf(ds, "name",     "Unnamed");
                        String price = valueOf(ds, "price",    "0");
                        String image = valueOf(ds, "image",    "");
                        String qty   = valueOf(ds, "quantity", "1");

                        Product p = new Product();
                        p.setId(ds.getKey());
                        p.setName(name);
                        p.setPrice(price);
                        p.setImageUrl(image);
                        p.setQuantity(qty);
                        cartList.add(p);

                        int    qtyInt  = parseIntSafe(qty, 1);
                        double priceD  = parseDoubleSafe(price, 0);
                        totalAmount   += priceD * qtyInt;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 🛠️ પરફોર્મન્સ ફિક્સ: લૂપની અંદર વારંવાર સેટ કરવાને બદલે ડેટા અપડેટ કરો
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                // Format total with 2 decimal places
                txtTotalAmount.setText(String.format("₹ %.2f", totalAmount));

                // Show/hide empty state
                boolean empty = cartList.isEmpty();
                txtEmptyCart.setVisibility(empty ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(empty ? View.GONE  : View.VISIBLE);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this,
                        "Failed to load cart: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Drawer navigation ─────────────────────────────────────────────
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(this, MyOrdersActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ── Back press ────────────────────────────────────────────────────
    @Override
    public void onBackPressed() {
        if (drawerLayout != null
                && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ── Utility helpers ───────────────────────────────────────────────
    private String valueOf(DataSnapshot ds, String key, String fallback) {
        Object val = ds.child(key).getValue();
        if (val == null) return fallback;
        String s = String.valueOf(val);
        return "null".equals(s) ? fallback : s;
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (Exception e) { return fallback; }
    }

    private double parseDoubleSafe(String s, double fallback) {
        try { return Double.parseDouble(s); } catch (Exception e) { return fallback; }
    }
}