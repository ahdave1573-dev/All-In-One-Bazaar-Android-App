package com.example.all_in_one_bazaar.ui.client.home;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.BannerAdapter;
import com.example.all_in_one_bazaar.ui.client.adapter.UserCategoryAdapter;
import com.example.all_in_one_bazaar.ui.client.adapter.HorizontalProductAdapter;
import com.example.all_in_one_bazaar.model.Category;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.auth.LoginActivity;
import com.example.all_in_one_bazaar.ui.client.cart_checkout.CartActivity;
import com.example.all_in_one_bazaar.ui.client.category.CategoryProductsActivity;
import com.example.all_in_one_bazaar.ui.client.order.MyOrdersActivity;
import com.example.all_in_one_bazaar.ui.client.profile.ProfileActivity;
import com.example.all_in_one_bazaar.ui.client.features.NotificationActivity;
import com.example.all_in_one_bazaar.ui.client.features.SearchActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ImageView imgTopProfile, imgMenu, imgNotification;
    TextView txtViewAllCategories;
    LinearLayout bottomNavHome, bottomNavCat, bottomNavCart, bottomNavOrder, bottomNavProfile;
    View homeSearchBar;
    RecyclerView recyclerHomeCategories, recyclerFlashSale;
    androidx.viewpager2.widget.ViewPager2 viewPagerBanner;

    FirebaseAuth auth;
    DatabaseReference userRef, categoryRef, productRef;

    DatabaseReference notifRef;
    com.google.firebase.database.Query notifQuery;
    ChildEventListener notifListener;

    private static final String CHANNEL_ID = "bazaar_headsup_v3";
    private static final int NOTIF_REQ_CODE = 101;

    List<Category> categoryList;
    List<Product> topProductList;
    List<Product> flashSaleList;
    List<Product> bannerOfferList; // 🛠️ ૪૦% કે તેથી વધુ ઑફર માટે નવું સેફ લિસ્ટ

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    android.os.Handler sliderHandler = new android.os.Handler();
    Runnable sliderRunnable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Bind Views
        imgTopProfile = findViewById(R.id.imgTopProfile);
        imgMenu = findViewById(R.id.imgMenu);
        imgNotification = findViewById(R.id.imgNotification);
        txtViewAllCategories = findViewById(R.id.txtViewAllCategories);
        homeSearchBar = findViewById(R.id.homeSearchBar);
        bottomNavHome = findViewById(R.id.bottomNavHome);
        bottomNavCat = findViewById(R.id.bottomNavCat);
        bottomNavCart = findViewById(R.id.bottomNavCart);
        bottomNavOrder = findViewById(R.id.bottomNavOrder);
        bottomNavProfile = findViewById(R.id.bottomNavProfile);
        recyclerHomeCategories = findViewById(R.id.recyclerHomeCategories);
        viewPagerBanner = findViewById(R.id.viewPagerBanner);

        // Flash Sale RecyclerView Setup (Horizontal Layout)
        recyclerFlashSale = findViewById(R.id.recyclerTopRatedProducts);
        if (recyclerFlashSale != null) {
            recyclerFlashSale.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        if (recyclerHomeCategories != null) {
            recyclerHomeCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        categoryList = new ArrayList<>();
        topProductList = new ArrayList<>();
        flashSaleList = new ArrayList<>();
        bannerOfferList = new ArrayList<>(); // Initialize banner list

        if (imgMenu != null && drawerLayout != null) {
            imgMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (txtViewAllCategories != null) {
            txtViewAllCategories.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryProductsActivity.class)));
        }

        if (imgNotification != null) {
            imgNotification.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));
        }
        if (homeSearchBar != null) {
            homeSearchBar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        }

        checkNotificationPermission();

        // Bottom Navigation Listeners
        if (bottomNavCat != null) bottomNavCat.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CategoryProductsActivity.class)));
        if (bottomNavCart != null) bottomNavCart.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
        if (bottomNavOrder != null) bottomNavOrder.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyOrdersActivity.class)));
        if (bottomNavProfile != null) bottomNavProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        if (imgTopProfile != null) imgTopProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        if (currentUser != null) {
            loadUserProfile(currentUser.getUid());
            loadCategories();
            loadTopRatedProducts();
            getAndSaveFCMToken(currentUser.getUid());
            listenForNewNotifications(currentUser.getUid());
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        setupAutoSlider();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIF_REQ_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIF_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification Alert Enabled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getAndSaveFCMToken(String userId) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(userId).child("fcmToken").setValue(token);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserProfile(String uid) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (navigationView != null) {
                        View headerView = navigationView.getHeaderView(0);
                        if (headerView != null) {
                            ImageView imgNavProfile = headerView.findViewById(R.id.imgNavProfile);
                            TextView txtNavName = headerView.findViewById(R.id.txtNavName);
                            TextView txtNavEmail = headerView.findViewById(R.id.txtNavEmail);

                            if (name != null && txtNavName != null) txtNavName.setText(name);
                            if (email != null && txtNavEmail != null) txtNavEmail.setText(email);

                            String imageStr = snapshot.child("image").getValue(String.class);
                            if (imageStr != null && !imageStr.isEmpty()) {
                                try {
                                    if (imageStr.contains(",")) imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                                    byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    if (decodedByte != null) {
                                        if (imgTopProfile != null) imgTopProfile.setImageBitmap(decodedByte);
                                        if (imgNavProfile != null) imgNavProfile.setImageBitmap(decodedByte);
                                    }
                                } catch (Exception e) { e.printStackTrace(); }
                            }
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCategories() {
        categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Category cat = new Category();
                    cat.setName(ds.child("name").getValue(String.class));
                    cat.setImage(ds.child("image").getValue(String.class));

                    if (cat.getName() != null) categoryList.add(cat);
                }
                if (recyclerHomeCategories != null) {
                    recyclerHomeCategories.setAdapter(new UserCategoryAdapter(MainActivity.this, categoryList, categoryName -> {
                        Intent intent = new Intent(MainActivity.this, CategoryProductsActivity.class);
                        intent.putExtra("categoryName", categoryName);
                        startActivity(intent);
                    }));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadTopRatedProducts() {
        productRef = FirebaseDatabase.getInstance().getReference("products");
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topProductList.clear();
                flashSaleList.clear();
                bannerOfferList.clear(); // Clear banner list

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product p = new Product();
                    p.setId(ds.getKey());
                    p.setName(String.valueOf(ds.child("name").getValue()));
                    p.setImageUrl(String.valueOf(ds.child("image").getValue()));

                    Object priceObj = ds.child("sellingPrice").getValue();
                    if (priceObj == null || priceObj.toString().equals("null") || priceObj.toString().isEmpty()) {
                        priceObj = ds.child("price").getValue();
                    }
                    p.setPrice(priceObj != null ? String.valueOf(priceObj) : "0");

                    Object origObj = ds.child("originalPrice").getValue();
                    p.setOriginalPrice(origObj != null && !origObj.toString().equals("null") ? String.valueOf(origObj) : "");

                    p.setStockStatus(ds.child("stockStatus").getValue(String.class));
                    p.setCategory(ds.child("category").getValue(String.class));
                    p.setRating(String.valueOf(ds.child("rating").getValue()));

                    topProductList.add(p);

                    // ── 🛠️ FIX: ૪૦% કે તેથી વધુ ડિસ્કાઉન્ટ ફિલ્ટર લોજિક ──
                    String sPrice = p.getPrice();
                    String oPrice = p.getOriginalPrice();
                    if (sPrice != null && oPrice != null && !sPrice.isEmpty() && !oPrice.isEmpty() && !sPrice.equals("null") && !oPrice.equals("null")) {
                        try {
                            double sp = Double.parseDouble(sPrice);
                            double op = Double.parseDouble(oPrice);
                            if (op > sp && op > 0) {
                                int discountPercent = (int) (((op - sp) / op) * 100);
                                if (discountPercent >= 40) { // 🚀 ૪૦% કે તેથી વધુ હોય તો જ બેનરમાં જશે
                                    bannerOfferList.add(p);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }

                // Flash Sale માટે રેટિંગ સોર્ટિંગ લોજિક (ટોપ ૫)
                List<Product> sortedProducts = new ArrayList<>(topProductList);
                Collections.sort(sortedProducts, new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        double r1 = 0, r2 = 0;
                        try { r1 = Double.parseDouble(p1.getRating()); } catch (Exception e) { r1 = 0; }
                        try { r2 = Double.parseDouble(p2.getRating()); } catch (Exception e) { r2 = 0; }
                        return Double.compare(r2, r1);
                    }
                });

                for (int i = 0; i < sortedProducts.size() && i < 5; i++) {
                    flashSaleList.add(sortedProducts.get(i));
                }

                if (recyclerFlashSale != null) {
                    HorizontalProductAdapter flashSaleAdapter = new HorizontalProductAdapter(MainActivity.this, flashSaleList);
                    recyclerFlashSale.setAdapter(flashSaleAdapter);
                }

                // 🚀 FIXED ViewPager2 બેનર સેટઅપ: હવે બેનરમાં માત્ર ૪૦%+ વાળી પ્રોડક્ટ્સ જ પાસ થશે
                if (viewPagerBanner != null && !bannerOfferList.isEmpty()) {
                    BannerAdapter bannerAdapter = new BannerAdapter(MainActivity.this, bannerOfferList);
                    viewPagerBanner.setAdapter(bannerAdapter);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupAutoSlider() {
        sliderRunnable = () -> {
            if (viewPagerBanner != null && viewPagerBanner.getAdapter() != null) {
                int currentItem = viewPagerBanner.getCurrentItem();
                int itemCount = viewPagerBanner.getAdapter().getItemCount();
                if (itemCount > 0) viewPagerBanner.setCurrentItem((currentItem + 1) % itemCount, true);
            }
            sliderHandler.postDelayed(sliderRunnable, 3000);
        };
    }

    @Override protected void onPause() { super.onPause(); sliderHandler.removeCallbacks(sliderRunnable); }
    @Override protected void onResume() { super.onResume(); sliderHandler.postDelayed(sliderRunnable, 3000); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (notifQuery != null && notifListener != null) {
                notifQuery.removeEventListener(notifListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForNewNotifications(String uid) {
        notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(uid);

        notifRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastKey = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    lastKey = child.getKey();
                }

                if (lastKey != null) {
                    attachNotifListener(notifRef.orderByKey().startAfter(lastKey));
                } else {
                    attachNotifListener(notifRef.orderByKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NOTIF", "Failed to read last notification key", error.toException());
            }
        });
    }

    private void attachNotifListener(com.google.firebase.database.Query query) {
        notifQuery = query;

        notifListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                if (!snapshot.exists()) return;

                String title   = snapshot.child("title").getValue(String.class);
                String message = snapshot.child("message").getValue(String.class);

                if (title == null || title.isEmpty())   title   = "All In One Bazaar";
                if (message == null || message.isEmpty()) message = "You have a new notification!";

                showPopupNotification(title, message);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String prev) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String prev) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };

        notifQuery.addChildEventListener(notifListener);
    }

    private void showPopupNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, "Bazaar Order Alerts", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Real-time notifications for your orders and updates");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 500, 200, 500});
                channel.enableLights(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                AudioAttributes audioAttr = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttr);
                manager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int notifId = (int) System.currentTimeMillis();

        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent contentIntent = PendingIntent.getActivity(this, notifId, intent, pendingFlags);
        PendingIntent fullScreenIntent = PendingIntent.getActivity(this, notifId + 1, intent, pendingFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setContentIntent(contentIntent)
                .setFullScreenIntent(fullScreenIntent, true);

        manager.notify(notifId, builder.build());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
        else if (id == R.id.nav_orders) startActivity(new Intent(this, MyOrdersActivity.class));
        else if (id == R.id.nav_logout) {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}