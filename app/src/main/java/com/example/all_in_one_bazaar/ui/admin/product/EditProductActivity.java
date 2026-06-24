package com.example.all_in_one_bazaar.ui.admin.product;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.all_in_one_bazaar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditProductActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private CardView btnBack;

    // NEW FIELDS INCLUDED HERE
    private EditText edtName, edtPrice, edtEditShortDescription, edtEditLongDescription, edtQuantity, edtRating;

    private Spinner spinnerCategory;
    private Button btnUpdate, btnPickImage;

    private DatabaseReference productsRef;
    private String productId;
    private Bitmap newBitmap = null;
    private String existingImageBase64 = "";
    private static final int GALLERY_REQUEST = 201;

    // ડાયનેમિક કેટેગરી માટેના વેરીએબલ્સ
    private List<String> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private String selectedCategoryFromDB = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Get product ID from intent
        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Error: No product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init views
        btnBack = findViewById(R.id.btnBackEdit);
        imgPreview = findViewById(R.id.imgEditPreview);
        edtName = findViewById(R.id.edtEditName);
        edtPrice = findViewById(R.id.edtEditPrice);

        // NEW DESCRIPTIONS VIEWS
        edtEditShortDescription = findViewById(R.id.edtEditShortDescription);
        edtEditLongDescription = findViewById(R.id.edtEditLongDescription);

        edtQuantity = findViewById(R.id.edtEditQuantity);
        edtRating = findViewById(R.id.edtEditRating);
        spinnerCategory = findViewById(R.id.spinnerEditCategory);
        btnUpdate = findViewById(R.id.btnUpdateProduct);
        btnPickImage = findViewById(R.id.btnPickNewImage);

        // Setup dynamic category spinner
        categoryList = new ArrayList<>();
        categoryList.add("-- Select Category --");
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Firebase ref
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        // ૧. પેલા ડેટાબેઝ માંથી કેટેગરી લોડ કરો
        loadCategoriesFromFirebase();

        // ૨. પછી પ્રોડક્ટનો ડેટા લોડ કરો
        loadProductData();

        // Click listeners
        btnBack.setOnClickListener(v -> finish());
        btnPickImage.setOnClickListener(v -> openGallery());
        imgPreview.setOnClickListener(v -> openGallery());
        btnUpdate.setOnClickListener(v -> updateProduct());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select New Image"), GALLERY_REQUEST);
    }

    private void loadCategoriesFromFirebase() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryList.add("-- Select Category --");

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = ds.child("name").getValue(String.class);
                    if (name != null && !name.trim().isEmpty()) {
                        categoryList.add(name);
                    }
                }
                categoryAdapter.notifyDataSetChanged();

                // જો પ્રોડક્ટ ડેટા વહેલો લોડ થઇ ગયો હોય, તો આ મેથડ સ્પિનરને સેટ કરશે
                setSpinnerSelection();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductData() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading product...");
        pd.show();

        productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pd.dismiss();
                if (!snapshot.exists()) {
                    Toast.makeText(EditProductActivity.this, "Product not found!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Populate fields
                String name = snapshot.child("name").getValue(String.class);
                Object priceObj = snapshot.child("price").getValue();
                if (priceObj == null) priceObj = snapshot.child("sellingPrice").getValue();
                String price = String.valueOf(priceObj);

                // NEW: Fetching descriptions
                String shortDesc = snapshot.child("shortDescription").getValue(String.class);
                String longDesc = snapshot.child("longDescription").getValue(String.class);
                String oldDesc = snapshot.child("description").getValue(String.class); // For older products

                String rating = snapshot.child("rating").getValue(String.class);
                String image = snapshot.child("image").getValue(String.class);
                Object qtyObj = snapshot.child("quantity").getValue();
                String qtyStr = String.valueOf(qtyObj);

                selectedCategoryFromDB = snapshot.child("category").getValue(String.class);

                if (name != null) edtName.setText(name);
                if (price != null && !price.equals("null")) edtPrice.setText(price);
                if (rating != null) edtRating.setText(rating);
                if (qtyStr != null && !qtyStr.equals("null")) edtQuantity.setText(qtyStr);

                // Set Description Logic (Fallback for old data)
                if (shortDesc != null && !shortDesc.isEmpty()) {
                    edtEditShortDescription.setText(shortDesc);
                } else if (oldDesc != null) {
                    edtEditShortDescription.setText(oldDesc);
                }

                if (longDesc != null && !longDesc.isEmpty()) {
                    edtEditLongDescription.setText(longDesc);
                } else if (oldDesc != null) {
                    edtEditLongDescription.setText(oldDesc);
                }

                // સ્પિનરમાં જૂની કેટેગરી સિલેક્ટ કરો
                setSpinnerSelection();

                // Load image
                existingImageBase64 = image;
                loadImageIntoPreview(image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
                Toast.makeText(EditProductActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerSelection() {
        if (selectedCategoryFromDB != null && !selectedCategoryFromDB.isEmpty()) {
            int index = categoryList.indexOf(selectedCategoryFromDB);
            if (index > 0) {
                spinnerCategory.setSelection(index);
            }
        }
    }

    private void loadImageIntoPreview(String imageStr) {
        try {
            if (imageStr != null && !imageStr.isEmpty() && !imageStr.equals("null")) {
                imgPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imgPreview.setImageTintList(null);
                imgPreview.setColorFilter(null);

                if (imageStr.startsWith("http")) {
                    Glide.with(this)
                            .load(imageStr)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(imgPreview);
                } else {
                    byte[] imageBytes = Base64.decode(imageStr, Base64.DEFAULT);
                    Glide.with(this)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(imgPreview);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                newBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgPreview.setImageBitmap(newBitmap);

                imgPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imgPreview.setImageTintList(null);
                imgPreview.setColorFilter(null);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProduct() {
        String name = edtName.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String shortDesc = edtEditShortDescription.getText().toString().trim();
        String longDesc = edtEditLongDescription.getText().toString().trim();
        String qty = edtQuantity.getText().toString().trim();
        String rating = edtRating.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Name is required");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating product...");
        pd.setCancelable(false);
        pd.show();

        // Determine image to save
        String imageToSave;
        if (newBitmap != null) {
            imageToSave = encodeImage(newBitmap);
        } else {
            imageToSave = (existingImageBase64 != null) ? existingImageBase64 : "";
        }

        // Build update map
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", name);
        updateMap.put("price", price);
        updateMap.put("sellingPrice", price);

        // NEW DESCRIPTIONS SAVING
        updateMap.put("shortDescription", shortDesc);
        updateMap.put("longDescription", longDesc);
        updateMap.put("description", shortDesc); // For backward compatibility

        updateMap.put("category", category.equals("-- Select Category --") ? "" : category);
        updateMap.put("image", imageToSave);
        updateMap.put("rating", rating.isEmpty() ? "0.0" : rating);

        int qtyInt = 0;
        try { qtyInt = Integer.parseInt(qty); } catch (Exception e) { qtyInt = 0; }
        updateMap.put("quantity", qtyInt);

        // Update stock status based on quantity
        updateMap.put("stockStatus", qtyInt > 0 ? "In Stock" : "Out of Stock");

        productsRef.child(productId).updateChildren(updateMap).addOnCompleteListener(task -> {
            pd.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Product updated successfully! ✅", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String encodeImage(Bitmap bmp) {
        int w = 800;
        int h = bmp.getHeight() * w / bmp.getWidth();
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, w, h, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
    }
}