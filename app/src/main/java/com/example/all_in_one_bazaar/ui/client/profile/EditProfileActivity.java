package com.example.all_in_one_bazaar.ui.client.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    ImageView imgProfile;
    EditText edtName, edtEmail;
    CardView btnUpdate, btnBack, cardProfileImage;

    FirebaseAuth auth;
    DatabaseReference userRef;
    String currentUserId;

    String encodedImage = "";
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        // 🛠️ Status Bar Background Color Fix (Image 2 મુજબ ઘાટો બ્લુ)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 🎨 Status Bar નો કલર Image 2 જેવો ઘાટો બ્લુ સેટ કર્યો છે
            window.setStatusBarColor(android.graphics.Color.parseColor("#1C52B2"));

            // 🔋 અક્ષરો અને આઈકન હંમેશા સફેદ (White) જ રહે તે માટેનું સેટિંગ
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White Icons
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.view.WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    insetsController.setSystemBarsAppearance(
                            0,
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    );
                }
            }
        }

        btnBack = findViewById(R.id.btnBack);
        imgProfile = findViewById(R.id.imgEditProfile);
        cardProfileImage = findViewById(R.id.cardProfileImage);
        edtName = findViewById(R.id.edtEditName);
        edtEmail = findViewById(R.id.edtEditEmail);
        btnUpdate = findViewById(R.id.btnUpdateProfile);

        btnBack.setOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            loadUserData();
        }

        cardProfileImage.setOnClickListener(v -> openGallery());
        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String img = snapshot.child("image").getValue(String.class);

                    if (name != null) edtName.setText(name);
                    if (email != null) edtEmail.setText(email);

                    if (img != null && !img.isEmpty() && !img.equals("null")) {
                        encodedImage = img;
                        try {
                            byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inJustDecodeBounds = true;
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
                            opts.inSampleSize = calculateInSampleSize(opts, 300, 300);
                            opts.inJustDecodeBounds = false;

                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
                            if (decodedByte != null) {
                                imgProfile.setImageBitmap(decodedByte);
                            } else {
                                imgProfile.setImageResource(android.R.drawable.sym_def_app_icon);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            imgProfile.setImageResource(android.R.drawable.sym_def_app_icon);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgProfile.setImageBitmap(bitmap);

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                byte[] bytes = stream.toByteArray();
                encodedImage = Base64.encodeToString(bytes, Base64.NO_WRAP);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfile() {
        String newName = edtName.getText().toString().trim();

        if (newName.isEmpty()) {
            edtName.setError("Name is required");
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
            user.updateProfile(profileUpdates);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("image", encodedImage);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully! 🎉", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this, "Update Failed.", Toast.LENGTH_SHORT).show();
            }
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
}