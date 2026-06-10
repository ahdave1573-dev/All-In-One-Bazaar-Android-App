package com.example.all_in_one_bazaar.ui.client.product;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.HorizontalProductAdapter;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.client.cart_checkout.CartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    ImageView imgProduct, btnWishlist;
    TextView txtName, txtPrice, txtDesc, txtCategory, txtStock, txtRating, txtDiscount;
    CardView btnShare, btnAddToCart, btnBuyNow, btnBack;

    // ── Another Products Views & Data ─────────────────────────────────
    RecyclerView recyclerAnotherProducts;
    HorizontalProductAdapter anotherProductAdapter;
    List<Product> anotherProductList;

    String productId, productName, productPrice, productDesc, imageUrl;
    boolean isInWishlist = false;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference wishlistRef, productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // ── 🛠️ FIX: સ્ટેટસ બારનો કલર લાઈટ સ્કાય બ્લુ અને આઇકોન્સ વ્હાઇટ (સફેદ) રાખવાનું લોજિક ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 🎨 તમારા છેલ્લા સ્ક્રીનશોટ મુજબનો લાઈટ સ્કાય બ્લુ કલર સેટ કર્યો
            window.setStatusBarColor(android.graphics.Color.parseColor("#E3F2FD"));

            // આનાથી બેકગ્રાઉન્ડ લાઈટ હોવા છતાં આઇકોન્સ સફેદ (White) જ રહેશે
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White icons/text on status bar
            }
        }

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Bind Views
        imgProduct = findViewById(R.id.imgDetailProduct);
        btnWishlist = findViewById(R.id.btnWishlist);
        btnShare = findViewById(R.id.btnShare);
        btnBack = findViewById(R.id.btnBack);
        txtName = findViewById(R.id.txtDetailName);
        txtPrice = findViewById(R.id.txtDetailPrice);
        txtDesc = findViewById(R.id.txtDetailDescription);
        txtCategory = findViewById(R.id.txtDetailCategory);
        txtStock = findViewById(R.id.txtDetailStock);
        txtRating = findViewById(R.id.txtDetailRating);
        txtDiscount = findViewById(R.id.txtDetailDiscount);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        // Another Products RecyclerView Setup
        recyclerAnotherProducts = findViewById(R.id.recyclerAnotherProducts);
        recyclerAnotherProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        anotherProductList = new ArrayList<>();

        anotherProductAdapter = new HorizontalProductAdapter(this, anotherProductList);
        recyclerAnotherProducts.setAdapter(anotherProductAdapter);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            productId = intent.getStringExtra("id");
        }

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Error: Product ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails();

        if (user != null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(user.getUid());
            checkWishlistStatus();
        }

        btnBack.setOnClickListener(v -> finish());

        btnWishlist.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(this, "Please Login first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isInWishlist) removeFromWishlist();
            else addToWishlist();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (txtStock.getText().toString().equalsIgnoreCase("Out of Stock")) {
                Toast.makeText(this, "Product is Out of Stock!", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart();
        });

        btnBuyNow.setOnClickListener(v -> {
            if (txtStock.getText().toString().equalsIgnoreCase("Out of Stock")) {
                Toast.makeText(this, "Product is Out of Stock!", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart();
            startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
        });

        btnShare.setOnClickListener(v -> {
            String shareName = (productName != null) ? productName : "a product";
            String sharePrice = (productPrice != null) ? productPrice : "N/A";
            String shareBody = "Check out " + shareName + " on All In One Bazaar!\nPrice: ₹" + sharePrice;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Amazing Product");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }

    private void loadProductDetails() {
        productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ProductDetailActivity.this, "Product not found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                productName = snapshot.child("name").getValue(String.class);
                imageUrl = snapshot.child("image").getValue(String.class);

                String longDesc = snapshot.child("longDescription").getValue(String.class);
                String oldDesc = snapshot.child("description").getValue(String.class);
                productDesc = (longDesc != null && !longDesc.trim().isEmpty()) ? longDesc : oldDesc;

                Object priceObj = snapshot.child("price").getValue();
                if (priceObj == null) priceObj = snapshot.child("sellingPrice").getValue();
                productPrice = (priceObj != null) ? String.valueOf(priceObj) : "0";

                Object origPriceObj = snapshot.child("originalPrice").getValue();
                String originalPrice = (origPriceObj != null) ? String.valueOf(origPriceObj) : "0";

                String cat = snapshot.child("category").getValue(String.class);
                String rat = snapshot.child("rating").getValue(String.class);
                String stock = snapshot.child("stockStatus").getValue(String.class);

                txtName.setText(productName != null ? productName : "N/A");
                txtPrice.setText("₹ " + (productPrice != null && !productPrice.equals("null") ? productPrice : "0"));

                if (productPrice != null && originalPrice != null && !productPrice.isEmpty() && !originalPrice.isEmpty() && !productPrice.equals("null") && !originalPrice.equals("null")) {
                    try {
                        double sp = Double.parseDouble(productPrice);
                        double op = Double.parseDouble(originalPrice);
                        if (op > sp && op > 0) {
                            int discountPercent = (int) (((op - sp) / op) * 100);
                            txtDiscount.setText(discountPercent + "% OFF");
                            txtDiscount.setVisibility(android.view.View.VISIBLE);
                        } else {
                            txtDiscount.setVisibility(android.view.View.GONE);
                        }
                    } catch (Exception e) {
                        txtDiscount.setVisibility(android.view.View.GONE);
                    }
                } else {
                    txtDiscount.setVisibility(android.view.View.GONE);
                }

                txtDesc.setText(productDesc != null ? productDesc : "No description available");
                txtCategory.setText(cat != null ? cat : "N/A");
                txtRating.setText((rat != null && !rat.equals("null") ? rat : "0.0") + " ★");

                if (cat != null) {
                    loadAnotherProducts(cat);
                }

                Object qtyObj = snapshot.child("quantity").getValue();
                int quantity = (qtyObj != null) ? Integer.parseInt(String.valueOf(qtyObj)) : 0;

                boolean isInStock = false;
                if (stock != null && !stock.equals("null") && !stock.trim().isEmpty()) {
                    String cleanStock = stock.trim().toLowerCase();
                    if (cleanStock.equals("in stock") || cleanStock.equals("available")) {
                        isInStock = true;
                    }
                }
                if (quantity > 0) isInStock = true;

                if (isInStock) {
                    txtStock.setText("In Stock");
                    txtStock.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    txtStock.setText("Out of Stock");
                    txtStock.setTextColor(Color.parseColor("#F44336"));
                }

                try {
                    if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("null")) {
                        if (imageUrl.startsWith("http")) {
                            Glide.with(ProductDetailActivity.this)
                                    .load(imageUrl)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_gallery)
                                    .into(imgProduct);
                        } else {
                            byte[] imageByteArray = Base64.decode(imageUrl, Base64.DEFAULT);
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inJustDecodeBounds = true;
                            BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, opts);
                            opts.inSampleSize = calculateInSampleSize(opts, 500, 500);
                            opts.inJustDecodeBounds = false;
                            Bitmap decoded = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length, opts);
                            if (decoded != null) {
                                imgProduct.setImageBitmap(decoded);
                            } else {
                                imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        }
                    } else {
                        imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAnotherProducts(String categoryName) {
        DatabaseReference allProductsRef = FirebaseDatabase.getInstance().getReference("products");
        allProductsRef.orderByChild("category").equalTo(categoryName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        anotherProductList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            if (ds.getKey() != null && ds.getKey().equals(productId)) {
                                continue;
                            }

                            try {
                                Product p = new Product();
                                p.setId(ds.getKey());
                                p.setName(ds.child("name").getValue(String.class));

                                Object pObj = ds.child("price").getValue();
                                if (pObj == null) pObj = ds.child("sellingPrice").getValue();
                                p.setPrice(pObj != null ? String.valueOf(pObj) : "0");

                                Object opObj = ds.child("originalPrice").getValue();
                                p.setOriginalPrice(opObj != null ? String.valueOf(opObj) : "0");

                                p.setImageUrl(ds.child("image").getValue(String.class));
                                p.setDescription(ds.child("description").getValue(String.class));
                                p.setCategory(ds.child("category").getValue(String.class));
                                p.setRating(ds.child("rating").getValue(String.class));
                                p.setStockStatus(ds.child("stockStatus").getValue(String.class));

                                Object qObj = ds.child("quantity").getValue();
                                p.setQuantity(qObj != null ? String.valueOf(qObj) : "0");

                                anotherProductList.add(p);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        anotherProductAdapter.notifyDataSetChanged();
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

    private void checkWishlistStatus() {
        if (wishlistRef == null) return;
        wishlistRef.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isInWishlist = true;
                    btnWishlist.setColorFilter(Color.parseColor("#F5A623"), PorterDuff.Mode.SRC_IN);
                } else {
                    isInWishlist = false;
                    btnWishlist.setColorFilter(Color.parseColor("#4A7FC1"), PorterDuff.Mode.SRC_IN);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addToWishlist() {
        if (wishlistRef == null) return;
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", productId);
        map.put("name", productName != null ? productName : "");
        map.put("price", productPrice != null ? productPrice : "0");
        map.put("image", imageUrl != null ? imageUrl : "");
        map.put("category", txtCategory.getText().toString());
        map.put("rating", txtRating.getText().toString().replace(" ★", ""));
        map.put("stockStatus", txtStock.getText().toString());
        wishlistRef.child(productId).setValue(map).addOnSuccessListener(u -> Toast.makeText(this, "Added to Wishlist ❤️", Toast.LENGTH_SHORT).show());
    }

    private void removeFromWishlist() {
        if (wishlistRef == null) return;
        wishlistRef.child(productId).removeValue().addOnSuccessListener(u -> Toast.makeText(this, "Removed from Wishlist 💔", Toast.LENGTH_SHORT).show());
    }

    private void addToCart() {
        if (user == null) {
            Toast.makeText(this, "Please Login first!", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());
        HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("productId", productId);
        cartMap.put("name", productName != null ? productName : "");
        cartMap.put("price", productPrice != null ? productPrice : "0");
        cartMap.put("image", imageUrl != null ? imageUrl : "");
        cartMap.put("quantity", 1);
        cartRef.child(productId).setValue(cartMap).addOnSuccessListener(v -> Toast.makeText(this, "Added to Cart 🛒", Toast.LENGTH_SHORT).show());
    }
}