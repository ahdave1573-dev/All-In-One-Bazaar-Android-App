package com.example.all_in_one_bazaar.ui.client.cart_checkout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.order.MyOrdersActivity;

import java.util.Locale;

public class OrderSuccessActivity extends AppCompatActivity {

    private TextView txtSuccessMessage, txtAmountPaid, txtPaymentMethod, txtOrderInfo;
    private CardView btnViewOrders, btnContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // 🛠️ Status Bar Fix (ડાર્ક બ્લુ સ્ટેટસ બાર અને સફેદ અક્ષરો માટે)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // 🎨 ડાર્ક બ્લુ કલર
            window.setStatusBarColor(android.graphics.Color.parseColor("#1C52B2"));

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // 0 = White text
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

        // Init views
        txtSuccessMessage = findViewById(R.id.txtSuccessMessage);
        txtAmountPaid = findViewById(R.id.txtAmountPaid);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtOrderInfo = findViewById(R.id.txtOrderInfo);
        btnViewOrders = findViewById(R.id.btnViewOrders);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);

        // Get intent data
        double totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
        String paymentMethod = getIntent().getStringExtra("paymentMethod");

        // Set data
        txtAmountPaid.setText("₹ " + String.format(Locale.getDefault(), "%.2f", totalAmount));

        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            txtPaymentMethod.setText("Payment: " + paymentMethod);
        } else {
            txtPaymentMethod.setText("Payment: Online");
        }

        txtOrderInfo.setText("Your order is being processed.\nYou will receive updates shortly.");

        // CardView: View My Orders
        btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MyOrdersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // CardView: Continue Shopping
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Go to home instead of checkout
        Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}