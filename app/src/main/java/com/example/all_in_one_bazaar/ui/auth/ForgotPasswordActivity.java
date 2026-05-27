package com.example.all_in_one_bazaar.ui.auth; // તમારું પ્રોપર પેકેજ પાથ રાખજો

import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private CardView btnSendLink, btnBack;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // 🛠️ Status Bar Fix (ડાર્ક બ્લુ સ્ટેટસ બાર અને સફેદ અક્ષરો માટે)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#1C52B2"));

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White text
            }
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind Views
        edtEmail = findViewById(R.id.edtForgotEmail);
        btnSendLink = findViewById(R.id.btnSendResetLink);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBarForgot);

        // Click Listeners
        btnBack.setOnClickListener(v -> finish());
        btnSendLink.setOnClickListener(v -> handlePasswordReset());
    }

    private void handlePasswordReset() {
        String emailStr = edtEmail.getText().toString().trim();

        // 🛠️ Validations
        if (emailStr.isEmpty()) {
            edtEmail.setError("Email address is required!");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            edtEmail.setError("Please enter a valid email address!");
            edtEmail.requestFocus();
            return;
        }

        // Show Loader & Disable Button
        progressBar.setVisibility(View.VISIBLE);
        btnSendLink.setEnabled(false);

        // 🔥 Firebase API Call for Password Reset Email
        mAuth.sendPasswordResetEmail(emailStr).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            btnSendLink.setEnabled(true);

            if (task.isSuccessful()) {
                // જો ઈમેઈલ સફળતાપૂર્વક મોકલાઈ જાય
                Toast.makeText(ForgotPasswordActivity.this, "Reset link sent successfully! Check your email. 🎉", Toast.LENGTH_LONG).show();
                finish(); // મેઈલ ગયા પછી પેજ બંધ થઈ જશે
            } else {
                // જો કોઈ એરર આવે (દા.ત. આ ઈમેઈલ રજીસ્ટર ન હોય)
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email.";
                Toast.makeText(ForgotPasswordActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}