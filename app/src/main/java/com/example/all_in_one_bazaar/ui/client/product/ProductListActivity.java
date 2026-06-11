package com.example.all_in_one_bazaar.ui.client.product;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    UserProductAdapter adapter;
    List<Product> productList;
    DatabaseReference databaseReference;
    EditText searchInput;
    ProgressBar progressBar;
    CardView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // ── 🛠️ FIX 1: કીબોર્ડ ઓપન થાય ત્યારે લેઆઉટ ઓટોમેટિક કમ્પ્રેસ/રીસાઈઝ થાય તે માટે ──
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // ── 🛠️ FIX 2: સ્ટેટસ બારનો કલર લાઈટ સ્કાય બ્લુ અને આઇકોન્સ વ્હાઇટ (સફેદ) રાખવાનું લોજિક ──
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 🎨 લાઈટ સ્કાય બ્લુ થીમ કલર
            window.setStatusBarColor(android.graphics.Color.parseColor("#E3F2FD"));

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White text/icons on status bar
            }
        }

        recyclerView = findViewById(R.id.recyclerProductList);
        searchInput = findViewById(R.id.edtSearch);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();

        adapter = new UserProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        loadProducts();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        String name = ds.child("name").getValue(String.class);

                        Object priceObj = ds.child("price").getValue();
                        if (priceObj == null) priceObj = ds.child("sellingPrice").getValue();
                        String price = (priceObj != null) ? String.valueOf(priceObj) : "0";

                        String image = ds.child("image").getValue(String.class);

                        String shortDesc = ds.child("shortDescription").getValue(String.class);
                        String oldDesc = ds.child("description").getValue(String.class);
                        String finalShortDesc = (shortDesc != null && !shortDesc.trim().isEmpty()) ? shortDesc : oldDesc;

                        String category = ds.child("category").getValue(String.class);
                        String rating = ds.child("rating").getValue(String.class);
                        String stock = ds.child("stockStatus").getValue(String.class);

                        Object qtyObj = ds.child("quantity").getValue();
                        String qtyStr = String.valueOf(qtyObj);

                        Product product = new Product();
                        product.setId(ds.getKey());
                        product.setName(name);
                        product.setPrice(price);
                        product.setImageUrl(image);
                        product.setDescription(finalShortDesc);
                        product.setCategory(category);
                        product.setRating(rating);
                        product.setStockStatus(stock);

                        String safeQty = (qtyObj != null) ? qtyStr : "0";
                        product.setQuantity(safeQty);

                        productList.add(product);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductListActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product item : productList) {
            if (item.getName() != null && item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.setFilteredList(filteredList);
    }
}