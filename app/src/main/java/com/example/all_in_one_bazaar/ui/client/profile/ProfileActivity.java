package com.example.all_in_one_bazaar.ui.client.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.dashboard.AdminDashboardActivity;
import com.example.all_in_one_bazaar.ui.auth.LoginActivity;
import com.example.all_in_one_bazaar.ui.client.cart_checkout.CartActivity;
import com.example.all_in_one_bazaar.ui.client.category.CategoryProductsActivity;
import com.example.all_in_one_bazaar.ui.client.features.NotificationActivity; // 🔔 આ નવું ઈમ્પોર્ટ કર્યું
import com.example.all_in_one_bazaar.ui.client.order.MyOrdersActivity;
import com.example.all_in_one_bazaar.ui.client.features.WishlistActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView txtName, txtEmail;
    TextView btnOrders, btnCart, btnWishlist, btnEditProfile, btnAdminAddProduct, btnLogout;
    ImageView imgProfile, imgMenu, imgNotification; // 🔔 imgNotification નવું એડ કર્યું

    // Navigation Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LinearLayout bottomNavHome, bottomNavCat, bottomNavCart, bottomNavOrder, bottomNavProfile;

    FirebaseAuth auth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Views Find
        txtName = findViewById(R.id.txtProfileName);
        txtEmail = findViewById(R.id.txtProfileEmail);
        imgProfile = findViewById(R.id.imgProfile);
        imgMenu = findViewById(R.id.imgMenu);
        imgNotification = findViewById(R.id.imgNotification); // 🔔 આઈકનને XML આઈડી સાથે લિંક કર્યું

        btnOrders = findViewById(R.id.btnMyOrders);
        btnCart = findViewById(R.id.btnMyCart);
        btnWishlist = findViewById(R.id.btnWishlist);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnAdminAddProduct = findViewById(R.id.btnAdminAddProduct);
        btnLogout = findViewById(R.id.btnLogout);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        bottomNavHome = findViewById(R.id.bottomNavHome);
        bottomNavCat = findViewById(R.id.bottomNavCat);
        bottomNavCart = findViewById(R.id.bottomNavCart);
        bottomNavOrder = findViewById(R.id.bottomNavOrder);
        bottomNavProfile = findViewById(R.id.bottomNavProfile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // 2. Load Data
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                txtName.setText(user.getDisplayName());
            }
            if (user.getEmail() != null) {
                txtEmail.setText(user.getEmail());
            }
            loadUserInfoFromDb(user.getUid());
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // 3. Navigation Clicks
        imgMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(this);

        // 🔔 FIX: ટોપમાં નોટિફિકેશન આઇકન પર ક્લિક કરવાથી લિંક ઓપન થશે
        if (imgNotification != null) {
            imgNotification.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Nav Clicks
        bottomNavHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        bottomNavCat.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryProductsActivity.class));
            finish();
        });
        bottomNavCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });
        bottomNavOrder.setOnClickListener(v -> {
            startActivity(new Intent(this, MyOrdersActivity.class));
            finish();
        });
        bottomNavProfile.setOnClickListener(v -> Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show());

        // 4. Action Button Clicks
        if (btnCart != null) btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        if (btnOrders != null) btnOrders.setOnClickListener(v -> startActivity(new Intent(this, MyOrdersActivity.class)));
        if (btnWishlist != null) btnWishlist.setOnClickListener(v -> startActivity(new Intent(this, WishlistActivity.class)));
        if (btnEditProfile != null) btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));

        if (btnAdminAddProduct != null) {
            btnAdminAddProduct.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class)));
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfoFromDb(user.getUid());
        }
    }

    private void loadUserInfoFromDb(String uid) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() { // 🛠️ Real-time અપડેટ રાખવા addValueEventListener કર્યું
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String imageStr = snapshot.child("image").getValue(String.class);

                    if (name != null) txtName.setText(name);

                    // 🛠️ SECURITY FIX: હવે હાર્ડકોડેડ ઈમેઈલ નહી, પણ ડેટાબેઝના 'role' મુજબ એડમિન બટન દેખાશે
                    if (btnAdminAddProduct != null) {
                        if ("admin".equals(role)) {
                            btnAdminAddProduct.setVisibility(View.VISIBLE);
                        } else {
                            btnAdminAddProduct.setVisibility(View.GONE);
                        }
                    }

                    // Update Nav Drawer Header as well
                    View headerView = navigationView.getHeaderView(0);
                    if (headerView != null) {
                        TextView txtNavName = headerView.findViewById(R.id.txtNavName);
                        TextView txtNavEmail = headerView.findViewById(R.id.txtNavEmail);
                        ImageView imgNavProfile = headerView.findViewById(R.id.imgNavProfile);

                        if (name != null && txtNavName != null) txtNavName.setText(name);
                        if (snapshot.child("email").getValue(String.class) != null && txtNavEmail != null)
                            txtNavEmail.setText(snapshot.child("email").getValue(String.class));

                        // Decode and set image
                        if (imageStr != null && !imageStr.isEmpty() && !imageStr.equals("null")) {
                            try {
                                if (imageStr.contains(",")) imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                                byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);

                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inJustDecodeBounds = true;
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
                                opts.inSampleSize = calculateInSampleSize(opts, 200, 200);
                                opts.inJustDecodeBounds = false;

                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
                                if (decodedByte != null) {
                                    imgProfile.setImageBitmap(decodedByte);
                                    if (imgNavProfile != null) {
                                        imgNavProfile.setColorFilter(null);
                                        imgNavProfile.setPadding(0, 0, 0, 0);
                                        imgNavProfile.setImageBitmap(decodedByte);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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

    // --- DRAWER MENU CLICKS ---
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_profile) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(ProfileActivity.this, MyOrdersActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            auth.signOut();
            Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
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
}