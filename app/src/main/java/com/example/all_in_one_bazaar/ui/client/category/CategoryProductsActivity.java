package com.example.all_in_one_bazaar.ui.client.category;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.UserCategoryAdapter;
import com.example.all_in_one_bazaar.model.Category;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.auth.LoginActivity;
import com.example.all_in_one_bazaar.ui.client.cart_checkout.CartActivity;
import com.example.all_in_one_bazaar.ui.client.features.NotificationActivity;
import com.example.all_in_one_bazaar.ui.client.order.MyOrdersActivity;
import com.example.all_in_one_bazaar.ui.client.adapter.UserProductAdapter;
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

public class CategoryProductsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView   recyclerCategories, recyclerProducts;
    private ProgressBar    progressBar;
    private TextView       txtTitle, txtSelectedCategory, txtNoProducts; // 🛠️ Added txtNoProducts for validation
    private EditText       edtSearchCategory;
    private LinearLayout   layoutProductSection;
    private DrawerLayout   drawerLayout;
    private NavigationView navigationView;
    private ImageView      imgMenu, imgTopProfile, imgNotification;

    private LinearLayout bottomNavHome, bottomNavCat, bottomNavCart, bottomNavOrder, bottomNavProfile;

    private DatabaseReference productsRef, categoryRef, userRef;
    private FirebaseAuth      auth;

    private final List<Category> categoryList         = new ArrayList<>();
    private final List<Category> filteredCategoryList = new ArrayList<>();
    private final List<Product>  productList          = new ArrayList<>();

    private UserProductAdapter  productAdapter;
    private UserCategoryAdapter categoryAdapter;
    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        bindViews();
        initFirebase();
        setupAdapters();
        setupListeners();

        if (currentUser != null) {
            loadUserProfile(currentUser.getUid());
            loadCategories();

            String passedCategory = getIntent().getStringExtra("categoryName");
            if (passedCategory != null && !passedCategory.isEmpty()) {
                selectedCategory = passedCategory;
                txtSelectedCategory.setText(passedCategory);
                loadProductsByCategory(passedCategory);
            } else {
                txtSelectedCategory.setText("All Products");
                loadAllProducts();
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void bindViews() {
        imgMenu              = findViewById(R.id.imgMenu);
        imgTopProfile        = findViewById(R.id.imgTopProfile);
        imgNotification      = findViewById(R.id.imgNotification);
        txtTitle             = findViewById(R.id.txtTitle);
        txtSelectedCategory  = findViewById(R.id.txtSelectedCategory);
        txtNoProducts        = findViewById(R.id.txtNoProducts); // ⚠️ Make sure to add this ID in activity_category_products.xml
        edtSearchCategory    = findViewById(R.id.edtSearchCategory);
        recyclerCategories   = findViewById(R.id.recyclerCategories);
        recyclerProducts     = findViewById(R.id.recyclerProducts);
        progressBar          = findViewById(R.id.progressBar);
        layoutProductSection = findViewById(R.id.layoutProductSection);
        drawerLayout         = findViewById(R.id.drawerLayout);
        navigationView       = findViewById(R.id.navigationView);

        bottomNavHome    = findViewById(R.id.bottomNavHome);
        bottomNavCat     = findViewById(R.id.bottomNavCat);
        bottomNavCart    = findViewById(R.id.bottomNavCart);
        bottomNavOrder   = findViewById(R.id.bottomNavOrder);
        bottomNavProfile = findViewById(R.id.bottomNavProfile);
    }

    private void initFirebase() {
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        categoryRef = FirebaseDatabase.getInstance().getReference("categories");
    }

    private void setupAdapters() {
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new UserCategoryAdapter(this, filteredCategoryList,
                categoryName -> {
                    selectedCategory = categoryName;
                    txtSelectedCategory.setText(categoryName);
                    loadProductsByCategory(categoryName); // Click થતા જ નવું ફિલ્ટર ટ્રિગર થશે
                });
        recyclerCategories.setAdapter(categoryAdapter);

        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new UserProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        imgMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else
                drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(this);
        imgTopProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        if (imgNotification != null) {
            imgNotification.setOnClickListener(v -> startActivity(new Intent(CategoryProductsActivity.this, NotificationActivity.class)));
        }

        edtSearchCategory.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                filterCategories(s.toString());
            }
        });

        bottomNavHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        bottomNavCat.setOnClickListener(v -> Toast.makeText(this, "Already on Categories", Toast.LENGTH_SHORT).show());
        bottomNavCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        bottomNavOrder.setOnClickListener(v -> startActivity(new Intent(this, MyOrdersActivity.class)));
        bottomNavProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Category cat = ds.getValue(Category.class);
                    if (cat != null) categoryList.add(cat);
                }
                filteredCategoryList.clear();
                filteredCategoryList.addAll(categoryList);
                categoryAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Product p = parseProduct(ds);
                        if (p != null) productList.add(p);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                Collections.shuffle(productList);

                // 🛠️ UI Validation Update
                toggleNoDataView();

                productAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { progressBar.setVisibility(View.GONE); }
        });
    }

    private void loadProductsByCategory(String category) {
        progressBar.setVisibility(View.VISIBLE);
        // 🛠️ Optimization: કેટેગરી સ્વિચ થતી વખતે જૂનો ડેટા પહેલા જ ક્લિયર કરી દેવો જેથી રોન્ગ ડેટા ના ફ્લેશ થાય
        productList.clear();
        productAdapter.notifyDataSetChanged();

        productsRef.orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() { // 🚀 single value event for faster switching
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            try {
                                Product p = parseProduct(ds);
                                if (p != null) productList.add(p);
                            } catch (Exception e) { e.printStackTrace(); }
                        }

                        // 🛠️ UI Validation Update
                        toggleNoDataView();

                        productAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) { progressBar.setVisibility(View.GONE); }
                });
    }

    // 🛠️ NEW HELPER: ડેટા ખાલી હોય તો મેસેજ હેન્ડલ કરવા માટે
    private void toggleNoDataView() {
        if (productList.isEmpty()) {
            if (txtNoProducts != null) txtNoProducts.setVisibility(View.VISIBLE);
            recyclerProducts.setVisibility(View.GONE);
        } else {
            if (txtNoProducts != null) txtNoProducts.setVisibility(View.GONE);
            recyclerProducts.setVisibility(View.VISIBLE);
        }
    }

    private Product parseProduct(DataSnapshot ds) {
        String name  = ds.child("name").getValue(String.class);
        String price = safeString(ds, "price", "sellingPrice", "0");
        String image = ds.child("image").getValue(String.class);
        String desc  = ds.child("description").getValue(String.class);
        String cat   = ds.child("category").getValue(String.class);
        String origP = safeValue(ds, "originalPrice", "0");
        String rating = ds.child("rating").getValue(String.class);
        String stock  = ds.child("stockStatus").getValue(String.class);
        String qty    = safeValue(ds, "quantity", "0");

        Product p = new Product();
        p.setId(ds.getKey());
        p.setName(name);
        p.setPrice(price);
        p.setOriginalPrice(origP);
        p.setImageUrl(image);
        p.setDescription(desc);
        p.setCategory(cat);
        p.setRating(rating);
        p.setStockStatus(stock);
        p.setQuantity(qty);
        return p;
    }

    private void filterCategories(String query) {
        filteredCategoryList.clear();
        for (Category cat : categoryList) {
            if (cat.getName() != null && cat.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredCategoryList.add(cat);
            }
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private void loadUserProfile(String uid) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                View       header       = navigationView.getHeaderView(0);
                ImageView  imgNavProfile = header.findViewById(R.id.imgNavProfile);
                TextView   txtNavName   = header.findViewById(R.id.txtNavName);
                TextView   txtNavEmail  = header.findViewById(R.id.txtNavEmail);

                String name  = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                if (name  != null && txtNavName  != null) txtNavName.setText(name);
                if (email != null && txtNavEmail != null) txtNavEmail.setText(email);

                String imageStr = snapshot.child("image").getValue(String.class);
                if (imageStr != null && !imageStr.isEmpty() && !"null".equals(imageStr)) {
                    try {
                        if (imageStr.contains(",")) imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                        byte[] decoded = Base64.decode(imageStr.trim(), Base64.DEFAULT);

                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(decoded, 0, decoded.length, opts);
                        opts.inSampleSize = calculateInSampleSize(opts, 200, 200);
                        opts.inJustDecodeBounds = false;

                        Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length, opts);
                        if (bmp != null) {
                            if (imgTopProfile  != null) imgTopProfile.setImageBitmap(bmp);
                            if (imgNavProfile  != null) imgNavProfile.setImageBitmap(bmp);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(this, MyOrdersActivity.class));
        } else if (id == R.id.nav_logout) {
            auth.signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    private String safeString(DataSnapshot ds, String primary, String secondary, String def) {
        Object v = ds.child(primary).getValue();
        if (v == null) v = ds.child(secondary).getValue();
        return v != null ? String.valueOf(v) : def;
    }

    private String safeValue(DataSnapshot ds, String key, String def) {
        Object v = ds.child(key).getValue();
        return v != null ? String.valueOf(v) : def;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight, width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int hh = height / 2, hw = width / 2;
            while ((hh / inSampleSize) >= reqHeight && (hw / inSampleSize) >= reqWidth)
                inSampleSize *= 2;
        }
        return inSampleSize;
    }
}