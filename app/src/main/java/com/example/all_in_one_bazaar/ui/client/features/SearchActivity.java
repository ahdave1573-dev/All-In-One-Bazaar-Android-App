package com.example.all_in_one_bazaar.ui.client.features;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class SearchActivity extends AppCompatActivity {

    private CardView btnBack; // Changed to CardView
    private EditText edtSearchQuery;
    private RecyclerView recyclerSearchResults;
    private ProgressBar progressBar;
    private LinearLayout layoutNoResults; // Better empty state layout
    private TextView txtResultCount;

    private DatabaseReference productsRef;
    private List<Product> allProducts;
    private List<Product> filteredProducts;

    // 🔥 Changed to UserProductAdapter for theme consistency
    private UserProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Init views
        btnBack = findViewById(R.id.btnBack);
        edtSearchQuery = findViewById(R.id.edtSearchQuery);
        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        progressBar = findViewById(R.id.progressBar);
        layoutNoResults = findViewById(R.id.layoutNoResults);
        txtResultCount = findViewById(R.id.txtResultCount);

        // Setup
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();

        // Use 2 columns grid for products
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new UserProductAdapter(this, filteredProducts);
        recyclerSearchResults.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Auto-focus search box
        edtSearchQuery.requestFocus();

        // Search listener
        edtSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Load all products from Firebase
        loadAllProducts();
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allProducts.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        String name = ds.child("name").getValue(String.class);
                        Object priceObj = ds.child("price").getValue();
                        if (priceObj == null) priceObj = ds.child("sellingPrice").getValue();
                        String price = (priceObj != null) ? String.valueOf(priceObj) : "0";
                        String image = ds.child("image").getValue(String.class);
                        String desc = ds.child("description").getValue(String.class);
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
                        product.setDescription(desc);
                        product.setCategory(category);
                        product.setRating(rating);
                        product.setStockStatus(stock);

                        // quantity is String in Product model — pass qtyStr directly
                        String safeQty = (qtyObj != null) ? qtyStr : "0";
                        product.setQuantity(safeQty);

                        allProducts.add(product);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setVisibility(View.GONE);

                // If search query is already typed, filter immediately
                String currentQuery = edtSearchQuery.getText().toString();
                if (!currentQuery.isEmpty()) {
                    filterProducts(currentQuery);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredProducts.clear();

        if (query.trim().isEmpty()) {
            layoutNoResults.setVisibility(View.GONE);
            txtResultCount.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        for (Product product : allProducts) {
            boolean matches = false;

            if (product.getName() != null && product.getName().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            if (!matches && product.getCategory() != null && product.getCategory().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }
            if (!matches && product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery)) {
                matches = true;
            }

            if (matches) {
                filteredProducts.add(product);
            }
        }

        // Update UI states
        if (filteredProducts.isEmpty()) {
            layoutNoResults.setVisibility(View.VISIBLE);
            txtResultCount.setVisibility(View.GONE);
        } else {
            layoutNoResults.setVisibility(View.GONE);
            txtResultCount.setVisibility(View.VISIBLE);
            txtResultCount.setText(filteredProducts.size() + " result(s) found");
        }

        // Notify adapter about dataset changes
        adapter.notifyDataSetChanged();
    }
}