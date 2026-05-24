package com.example.all_in_one_bazaar.ui.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // ── UI Views ──────────────────────────────────────────────────────
    private EditText        edtName, edtEmail, edtPassword;
    private AppCompatButton btnRegister;
    private TextView        txtLoginLink;
    private ImageView       imgProfile, imgTogglePasswordReg;
    private CardView        btnUploadPhoto, btnBack;

    // ── Firebase ──────────────────────────────────────────────────────
    private FirebaseAuth      auth;
    private DatabaseReference userRef;

    // ── State ─────────────────────────────────────────────────────────
    private Uri     imageUri;
    private String  imageString      = "";
    private boolean isPasswordVisible = false;

    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🛠️ FIX: કીબોર્ડ ઓપન થાય ત્યારે લેઆઉટ ઓટોમેટિક એડજસ્ટ થાય અને સ્ક્રોલ વગર ફિક્સ રહે તે માટે
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Status Bar Fix (ડાર્ક બ્લુ સ્ટેટસ બાર અને સફેદ અક્ષરો માટે)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#1C52B2"));

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White text/icons
            }
        }

        FirebaseApp.initializeApp(this);

        bindViews();
        initFirebase();
        setupListeners();
    }

    // ── Bind Views ────────────────────────────────────────────────────
    private void bindViews() {
        imgProfile           = findViewById(R.id.imgProfile);
        btnUploadPhoto       = findViewById(R.id.btnUploadPhoto);
        btnBack              = findViewById(R.id.btnBack);
        edtName              = findViewById(R.id.edtRegName);
        edtEmail             = findViewById(R.id.edtRegEmail);
        edtPassword          = findViewById(R.id.edtRegPassword);
        btnRegister          = findViewById(R.id.btnRegister);
        txtLoginLink         = findViewById(R.id.txtLoginLink);
        imgTogglePasswordReg = findViewById(R.id.imgTogglePasswordReg);
    }

    // ── Firebase Init ─────────────────────────────────────────────────
    private void initFirebase() {
        auth    = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    // ── Listeners ─────────────────────────────────────────────────────
    private void setupListeners() {

        // Avatar & upload badge — both open gallery
        View.OnClickListener galleryListener = v -> openGallery();
        imgProfile.setOnClickListener(galleryListener);
        btnUploadPhoto.setOnClickListener(galleryListener);

        // Password eye toggle
        imgTogglePasswordReg.setOnClickListener(v -> togglePasswordVisibility());

        // Register
        btnRegister.setOnClickListener(v -> registerUser());

        // Go to Login
        txtLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Back
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ── Password Toggle ───────────────────────────────────────────────
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imgTogglePasswordReg.setImageResource(
                    android.R.drawable.ic_menu_close_clear_cancel);
            imgTogglePasswordReg.setColorFilter(
                    getResources().getColor(R.color.ocean_cta, getTheme()));
        } else {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgTogglePasswordReg.setImageResource(android.R.drawable.ic_menu_view);
            imgTogglePasswordReg.setColorFilter(
                    getResources().getColor(R.color.ocean_hint, getTheme()));
        }
        edtPassword.setSelection(edtPassword.getText().length());
    }

    // ── Gallery Picker ────────────────────────────────────────────────
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media
                        .getBitmap(getContentResolver(), imageUri);

                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 400, 400, true);

                imgProfile.clearColorFilter();
                imgProfile.setImageTintList(null);
                imgProfile.setPadding(0, 0, 0, 0);
                imgProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgProfile.setImageBitmap(resized);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                imageString = Base64.encodeToString(
                        stream.toByteArray(), Base64.NO_WRAP);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ── Register User ─────────────────────────────────────────────────
    private void registerUser() {
        String name     = edtName.getText().toString().trim();
        String email    = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("id",       uid);
                            userMap.put("name",     name);
                            userMap.put("email",    email);
                            userMap.put("image",    imageString);
                            userMap.put("role",     "user");

                            user.updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build()
                            );

                            userRef.child(uid).setValue(userMap)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(this,
                                                    "Registration Successful!",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(
                                                    RegisterActivity.this,
                                                    MainActivity.class));
                                            finishAffinity();
                                        } else {
                                            resetButton();
                                            Toast.makeText(this,
                                                    "Database Error: "
                                                            + dbTask.getException()
                                                            .getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        resetButton();
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration Failed";

                        // 🛠️ FIX: Toast.LONG ની જગ્યાએ Toast.LENGTH_LONG કરી દીધું
                        Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetButton() {
        btnRegister.setEnabled(true);
        btnRegister.setText("CREATE ACCOUNT");
    }
}