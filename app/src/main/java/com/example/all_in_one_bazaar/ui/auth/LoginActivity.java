package com.example.all_in_one_bazaar.ui.auth;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.dashboard.AdminDashboardActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // ── UI Views ──────────────────────────────────────────────────────
    EditText    edtEmail, edtPassword;
    ImageView   imgTogglePassword;
    TextView    txtRegister, txtForgotPassword;
    FrameLayout btnGoogleLogin;
    androidx.appcompat.widget.AppCompatButton btnLogin;

    // ── Firebase ──────────────────────────────────────────────────────
    FirebaseAuth      auth;
    DatabaseReference userRef;

    // ── Google Sign-In ────────────────────────────────────────────────
    GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 1001;

    // ── State ─────────────────────────────────────────────────────────
    boolean isPasswordVisible = false;

    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // કીબોર્ડ ઓપન થાય ત્યારે આખું પેજ ફિક્સ રહે અને ઓટોમેટિક રીસાઈઝ થાય તે માટે
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
        setupGoogleSignIn();
        setupListeners();
    }

    // ── Bind Views ────────────────────────────────────────────────────
    private void bindViews() {
        edtEmail          = findViewById(R.id.edtEmail);
        edtPassword       = findViewById(R.id.edtPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        btnLogin          = findViewById(R.id.btnLogin);
        txtRegister       = findViewById(R.id.txtRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        btnGoogleLogin    = findViewById(R.id.btnGoogleLogin);
    }

    // ── Firebase Init ─────────────────────────────────────────────────
    private void initFirebase() {
        auth    = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
    }

    // ── Listeners ─────────────────────────────────────────────────────
    private void setupListeners() {

        // Password toggle
        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility());

        // Email/Password login
        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass  = edtPassword.getText().toString().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            loginUser(email, pass);
        });

        // Google login
        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });

        // Register પેજ ઓપન કરવા માટે
        if (txtRegister != null)
            txtRegister.setOnClickListener(v ->
                    startActivity(new Intent(this, RegisterActivity.class)));

        // હવે જૂનું ડાયલોગ બોક્સ ખોલવાને બદલે આપણું નવું ForgotPasswordActivity પેજ ઓપન થશે
        if (txtForgotPassword != null) {
            txtForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }
    }

    // ── Password Toggle ───────────────────────────────────────────────
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imgTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            imgTogglePassword.setColorFilter(
                    getResources().getColor(R.color.ocean_cta, getTheme()));
        } else {
            edtPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imgTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
            imgTogglePassword.setColorFilter(
                    getResources().getColor(R.color.ocean_hint, getTheme()));
        }
        edtPassword.setSelection(edtPassword.getText().length());
    }

    // ── Email/Password Login ──────────────────────────────────────────
    private void loginUser(String email, String pass) {
        Toast.makeText(this, "Signing in…", Toast.LENGTH_SHORT).show();
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole();
                    } else {
                        String err = task.getException() != null
                                ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Role Check ────────────────────────────────────────────────────
    private void checkUserRole() {
        String uid = auth.getCurrentUser().getUid();
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.exists()
                        ? snapshot.child("role").getValue(String.class) : "user";

                if ("admin".equals(role)) {
                    Toast.makeText(LoginActivity.this,
                            "Welcome Admin!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,
                            AdminDashboardActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "DB Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Google Sign-In Setup ──────────────────────────────────────────
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Toast.makeText(this, "Signing in with Google…", Toast.LENGTH_SHORT).show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid   = auth.getCurrentUser().getUid();
                        String name  = auth.getCurrentUser().getDisplayName();
                        String email = auth.getCurrentUser().getEmail();

                        userRef.child(uid).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.exists()) {
                                            java.util.HashMap<String, String> map =
                                                    new java.util.HashMap<>();
                                            map.put("name",     name  != null ? name  : "");
                                            map.put("email",    email != null ? email : "");
                                            map.put("provider", "Google");
                                            map.put("role",     "user");
                                            userRef.child(uid).setValue(map);
                                        }
                                        Toast.makeText(LoginActivity.this,
                                                "Welcome, " + name + "!",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this,
                                                MainActivity.class));
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        startActivity(new Intent(LoginActivity.this,
                                                MainActivity.class));
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}