package com.example.all_in_one_bazaar.ui.admin.category;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.CategoryAdapter;
import com.example.all_in_one_bazaar.model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private CardView btnBack;
    private ImageView imgCategory;
    private EditText edtCategoryName;
    private Button btnAddCategory, btnCancelEdit;
    private RecyclerView recyclerCategories;

    private DatabaseReference categoryRef;
    private List<Category> categoryList;
    private CategoryAdapter adapter;

    private String base64Image = "";
    private String editingCategoryId = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        imgCategory.setImageBitmap(bitmap);

                        // ફોટો પસંદ કર્યા પછી પેડીંગ અને કલર કાઢી નાખશે
                        imgCategory.setImageTintList(null);
                        imgCategory.clearColorFilter();
                        imgCategory.setPadding(0, 0, 0, 0);

                        base64Image = encodeImage(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        // Init views
        btnBack = findViewById(R.id.btnBack);
        imgCategory = findViewById(R.id.imgCategory);
        edtCategoryName = findViewById(R.id.edtCategoryName);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        recyclerCategories = findViewById(R.id.recyclerCategories);

        // પેજ ખુલે ત્યારે કેમેરાનો આઈકોન મરૂન કલરમાં સેટ કરવા
        resetImageIcon();

        // Firebase Setup
        categoryRef = FirebaseDatabase.getInstance().getReference("categories");
        categoryList = new ArrayList<>();

        // RecyclerView Setup
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(this, categoryList, categoryRef);
        recyclerCategories.setAdapter(adapter);

        loadCategories();

        btnBack.setOnClickListener(v -> finish());

        imgCategory.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnAddCategory.setOnClickListener(v -> saveCategory());
        btnCancelEdit.setOnClickListener(v -> clearForm());
    }

    private void loadCategories() {
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Category category = ds.getValue(Category.class);
                        if (category != null) {
                            categoryList.add(category);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageCategoriesActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 400;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public void editCategory(Category category) {
        editingCategoryId = category.getId();
        edtCategoryName.setText(category.getName() != null ? category.getName() : "");
        btnAddCategory.setText("UPDATE CATEGORY");
        btnCancelEdit.setVisibility(View.VISIBLE);

        if (category.getImage() != null && !category.getImage().isEmpty()) {

            // ડેટાબેઝ માં ક્યારેક "data:image/jpeg;base64," એવું લખાઈને આવ્યું હોય તો કાઢી નાખવા
            String base64String = category.getImage();
            if(base64String.contains(",")){
                base64String = base64String.substring(base64String.indexOf(",") + 1);
            }
            base64Image = base64String;

            try {
                byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgCategory.setImageBitmap(decodedByte);

                // ડેટાબેઝ માંથી ફોટો લોડ થાય ત્યારે તેના પરથી 100% કલર કાઢી નાખવા
                imgCategory.setImageTintList(null);
                imgCategory.clearColorFilter();
                imgCategory.setPadding(0,0,0,0);
            } catch (Exception e) {
                resetImageIcon();
                e.printStackTrace();
            }
        } else {
            base64Image = "";
            resetImageIcon();
        }

        // Scroll to top
        findViewById(R.id.edtCategoryName).requestFocus();
    }

    private void clearForm() {
        editingCategoryId = null;
        edtCategoryName.setText("");
        base64Image = "";
        resetImageIcon();
        btnAddCategory.setText("ADD CATEGORY");
        btnCancelEdit.setVisibility(View.GONE);
    }

    private void resetImageIcon() {
        // કેમેરા આઈકોન પાછો લાવવા અને તેને મરૂન કલર આપવા
        imgCategory.setImageTintList(null);
        imgCategory.setImageResource(android.R.drawable.ic_menu_camera);
        imgCategory.setPadding(24, 24, 24, 24);
        imgCategory.setColorFilter(android.graphics.Color.parseColor("#900C3F")); // Ruby Red
    }

    private void saveCategory() {
        String categoryName = edtCategoryName.getText().toString().trim();

        if (TextUtils.isEmpty(categoryName)) {
            edtCategoryName.setError("Category name is required");
            edtCategoryName.requestFocus();
            return;
        }

        // Prevent Duplicate Names
        for (Category cat : categoryList) {
            if (cat.getName() != null && cat.getName().equalsIgnoreCase(categoryName)) {
                if (editingCategoryId == null || !editingCategoryId.equals(cat.getId())) {
                    Toast.makeText(this, "Category already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        String categoryId = editingCategoryId != null ? editingCategoryId : categoryRef.push().getKey();
        if (categoryId != null) {
            Category newCategory = new Category(categoryId, categoryName, base64Image);
            categoryRef.child(categoryId).setValue(newCategory)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageCategoriesActivity.this, editingCategoryId == null ? "Category Added!" : "Category Updated!", Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            Toast.makeText(ManageCategoriesActivity.this, "Failed to save category", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}