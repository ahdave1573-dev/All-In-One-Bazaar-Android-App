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

import com.example.all_in_one_bazaar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {

    private ImageView imgProductPreview;
    private EditText edtProductName, edtShortDescription, edtLongDescription, edtProductPrice, edtOriginalPrice, edtQuantity, edtRating;
    private Spinner spinnerCategory;
    private Button btnAddProduct, btnSaveDraft;
    private CardView btnBack, btnSaveTop, cardImagePicker;

    private DatabaseReference databaseReference;
    private Bitmap bitmap = null;
    private static final int GALLERY_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initViews();
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
        setupCategorySpinner();

        cardImagePicker.setOnClickListener(v -> openGallery());
        imgProductPreview.setOnClickListener(v -> openGallery());

        btnAddProduct.setOnClickListener(v -> uploadProduct(false));
        btnSaveDraft.setOnClickListener(v -> uploadProduct(true));
        btnSaveTop.setOnClickListener(v -> uploadProduct(false));
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        imgProductPreview = findViewById(R.id.imgProductPreview);
        cardImagePicker = findViewById(R.id.cardImagePicker);

        edtProductName = findViewById(R.id.edtProductName);
        edtShortDescription = findViewById(R.id.edtShortDescription);
        edtLongDescription = findViewById(R.id.edtLongDescription);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtOriginalPrice = findViewById(R.id.edtOriginalPrice);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtRating = findViewById(R.id.edtRating);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnBack = findViewById(R.id.btnBack);
        btnSaveTop = findViewById(R.id.btnSaveTop);
    }

    private void setupCategorySpinner() {
        java.util.List<String> categoryList = new java.util.ArrayList<>();
        categoryList.add("-- Select Category --");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (spinnerCategory != null) {
            spinnerCategory.setAdapter(adapter);
        }

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

                if (categoryList.size() == 1) {
                    categoryList.clear();
                    categoryList.add("No Category");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Product Image"), GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri imageUri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgProductPreview.setImageBitmap(bitmap);
                imgProductPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgProductPreview.setPadding(0, 0, 0, 0);
                imgProductPreview.setColorFilter(null);
                imgProductPreview.setImageTintList(null);
                cardImagePicker.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
                imgProductPreview.setBackground(null);
            } catch (IOException e) {
                e.printStackTrace();
                // ── 🛠️ FIXED: Toast message changed to English ──
                Toast.makeText(this, "Error loading image!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProduct(boolean isDraft) {
        String name = edtProductName.getText().toString().trim();
        String shortDesc = edtShortDescription.getText().toString().trim();
        String longDesc = edtLongDescription.getText().toString().trim();
        String sellingPrice = edtProductPrice.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        // ── 🛠️ FIXED: Validation text messages changed to English ──
        if (!isDraft) {
            if (TextUtils.isEmpty(name)) { edtProductName.setError("Enter product name"); return; }
            if (bitmap == null) { Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show(); return; }
            if (category.equals("-- Select Category --")) { Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show(); return; }
        }

        // ── 🛠️ FIXED: ProgressDialog text changed to English ──
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.show();

        String base64Image = (bitmap != null) ? encodeImage(bitmap) : "";
        String productId = databaseReference.push().getKey();

        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("id", productId);
        productMap.put("name", name);
        productMap.put("shortDescription", shortDesc);
        productMap.put("longDescription", longDesc);
        productMap.put("sellingPrice", sellingPrice);
        productMap.put("originalPrice", edtOriginalPrice.getText().toString().trim());
        productMap.put("image", base64Image);
        productMap.put("category", category);
        productMap.put("quantity", edtQuantity.getText().toString().trim());
        productMap.put("rating", edtRating.getText().toString().trim().isEmpty() ? "0.0" : edtRating.getText().toString().trim());
        productMap.put("status", isDraft ? "draft" : "active");
        productMap.put("timestamp", System.currentTimeMillis());

        if (productId != null) {
            databaseReference.child(productId).setValue(productMap).addOnCompleteListener(task -> {
                pd.dismiss();
                if (task.isSuccessful()) {
                    if (!isDraft) {
                        sendOfferNotificationToAll("New Offer! 🎉", "New product added: " + name + ". Check it out now!");
                    }
                    // ── 🛠️ FIXED: Success Toast changed to English ──
                    Toast.makeText(this, "Product uploaded successfully! 🎉", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendOfferNotificationToAll(String title, String message) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String userId = ds.getKey();
                    if (userId != null) {
                        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
                        String notifId = notifRef.push().getKey();
                        String timestamp = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());

                        HashMap<String, String> map = new HashMap<>();
                        map.put("id", notifId);
                        map.put("title", title);
                        map.put("message", message);
                        map.put("type", "offer");
                        map.put("timestamp", timestamp);

                        if (notifId != null) {
                            notifRef.child(notifId).setValue(map);
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
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