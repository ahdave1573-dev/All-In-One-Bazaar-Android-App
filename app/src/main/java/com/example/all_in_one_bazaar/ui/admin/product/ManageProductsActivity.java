package com.example.all_in_one_bazaar.ui.admin.product;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.adapter.ManageProductAdapter;
import com.example.all_in_one_bazaar.model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageProductAdapter adapter;
    private List<Product> productList;
    private List<Product> filteredList;
    private DatabaseReference productsRef;
    private ValueEventListener productListener;
    private ProgressBar progressBar;
    private TextView txtEmptyState, txtProductCount;
    private EditText edtSearch;
    private CardView btnBack;
    private FloatingActionButton fabAddProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);
        recyclerView = findViewById(R.id.recyclerManageProducts);
        progressBar = findViewById(R.id.progressBar);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        txtProductCount = findViewById(R.id.txtProductCount);
        fabAddProduct = findViewById(R.id.fabAddProduct);

        productsRef = FirebaseDatabase.getInstance().getReference("products");
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ManageProductAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        fabAddProduct.setOnClickListener(v ->
                startActivity(new Intent(ManageProductsActivity.this, AddProductActivity.class)));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        loadProducts();
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        productListener = productsRef.addValueEventListener(new ValueEventListener() {
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
                        String finalDesc = (shortDesc != null && !shortDesc.isEmpty()) ? shortDesc : oldDesc;

                        String category = ds.child("category").getValue(String.class);
                        String rating = ds.child("rating").getValue(String.class);
                        String stock = ds.child("stockStatus").getValue(String.class);

                        Object qtyObj = ds.child("quantity").getValue();
                        String qtyStr = (qtyObj != null) ? String.valueOf(qtyObj) : "0";

                        Product product = new Product();
                        product.setId(ds.getKey());
                        product.setName(name);
                        product.setPrice(price);
                        product.setImageUrl(image);
                        product.setDescription(finalDesc);
                        product.setCategory(category);
                        product.setRating(rating);
                        product.setStockStatus(stock);
                        product.setQuantity(qtyStr);

                        productList.add(product);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                txtProductCount.setText(productList.size() + " Products");

                String currentQuery = edtSearch.getText().toString();
                if (!currentQuery.isEmpty()) {
                    filterProducts(currentQuery);
                } else {
                    filteredList.clear();
                    filteredList.addAll(productList);
                    adapter.notifyDataSetChanged();
                }

                progressBar.setVisibility(View.GONE);

                if (productList.isEmpty()) {
                    txtEmptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    txtEmptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        String lowerQuery = query.toLowerCase();
        for (Product p : productList) {
            // ── 🛠️ સ્માર્ટ ફિલ્ટર: નામ અથવા કેટેગરી બંનેમાંથી કંઈ પણ સર્ચ કરી શકાશે ──
            if ((p.getName() != null && p.getName().toLowerCase().contains(lowerQuery)) ||
                    (p.getCategory() != null && p.getCategory().toLowerCase().contains(lowerQuery))) {
                filteredList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (productsRef != null && productListener != null) {
                productsRef.removeEventListener(productListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}