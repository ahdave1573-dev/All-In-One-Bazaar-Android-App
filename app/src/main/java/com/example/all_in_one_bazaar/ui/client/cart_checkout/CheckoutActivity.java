package com.example.all_in_one_bazaar.ui.client.cart_checkout;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.client.adapter.CartAdapter;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.client.order.MyOrdersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private CardView btnBack, btnConfirmOrder;
    private EditText edtFullName, edtPhone, edtAddress, edtCity, edtPincode;
    private RadioGroup radioGroupPayment;
    private RadioButton radioOnline, radioCOD;
    private RecyclerView recyclerOrderSummary;
    private TextView txtSubtotal, txtDelivery, txtTotalPayable;

    private List<Product> cartList;
    private CartAdapter cartAdapter;
    private double subtotal = 0;
    private static final double DELIVERY_CHARGE = 49.0;
    private double finalTotalAmount = 0;
    private DatabaseReference cartRef;
    private ValueEventListener cartListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // 🛠️ Status Bar Fix
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
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

        btnBack = findViewById(R.id.btnBack);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtCity = findViewById(R.id.edtCity);
        edtPincode = findViewById(R.id.edtPincode);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        radioOnline = findViewById(R.id.radioOnline);
        radioCOD = findViewById(R.id.radioCOD);
        recyclerOrderSummary = findViewById(R.id.recyclerOrderSummary);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtDelivery = findViewById(R.id.txtDelivery);
        txtTotalPayable = findViewById(R.id.txtTotalPayable);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);

        cartList = new ArrayList<>();
        recyclerOrderSummary.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(this, cartList);
        recyclerOrderSummary.setAdapter(cartAdapter);

        btnBack.setOnClickListener(v -> finish());

        loadUserInfo();
        loadCartSummary();

        btnConfirmOrder.setOnClickListener(v -> validateOrderAndProceed());
    }

    private void loadUserInfo() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("name")) edtFullName.setText(snapshot.child("name").getValue(String.class));
                    if (snapshot.hasChild("phone")) edtPhone.setText(snapshot.child("phone").getValue(String.class));
                    if (snapshot.hasChild("address")) edtAddress.setText(snapshot.child("address").getValue(String.class));
                    if (snapshot.hasChild("city")) edtCity.setText(snapshot.child("city").getValue(String.class));
                    if (snapshot.hasChild("pincode")) edtPincode.setText(snapshot.child("pincode").getValue(String.class));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadCartSummary() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);

        cartListener = cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                subtotal = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        String name = String.valueOf(ds.child("name").getValue());
                        String price = String.valueOf(ds.child("price").getValue());
                        String image = String.valueOf(ds.child("image").getValue());
                        String qtyStr = String.valueOf(ds.child("quantity").getValue());

                        Product product = new Product();
                        product.setId(ds.getKey());
                        product.setName(name);
                        product.setPrice(price);
                        product.setImageUrl(image);
                        product.setQuantity(qtyStr);

                        cartList.add(product);

                        int q = 1;
                        try { q = Integer.parseInt(qtyStr); } catch (Exception e) { q = 1; }
                        double p = Double.parseDouble(price);
                        subtotal += (p * q);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                cartAdapter.notifyDataSetChanged();

                txtSubtotal.setText("₹ " + String.format(Locale.getDefault(), "%.2f", subtotal));

                // 🔥 લોજિક ફિક્સ: જો સબટોટલ 5000 થી વધુ હોય તો ડિલિવરી FREE શો થશે
                if (subtotal <= 5000.0) {
                    txtDelivery.setText("₹ " + String.format(Locale.getDefault(), "%.2f", DELIVERY_CHARGE));
                    finalTotalAmount = subtotal + DELIVERY_CHARGE;
                } else {
                    txtDelivery.setText("FREE");
                    finalTotalAmount = subtotal; // કોઈ એક્સ્ટ્રા ચાર્જ નહિ ઉમેરાય
                }

                txtTotalPayable.setText("₹ " + String.format(Locale.getDefault(), "%.2f", finalTotalAmount));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void validateOrderAndProceed() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String city = edtCity.getText().toString().trim();
        String pincode = edtPincode.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty() || pincode.isEmpty()) {
            Toast.makeText(this, "Please fill all address fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPaymentId = radioGroupPayment.getCheckedRadioButtonId();

        if (selectedPaymentId == R.id.radioOnline) {
            showFakePaymentDialog(fullName, phone, address, city, pincode, "Online Payment");
        } else {
            processOrderWithLoading(fullName, phone, address, city, pincode, "Cash on Delivery", "COD_PENDING");
        }
    }

    private void showFakePaymentDialog(String name, String phone, String address, String city, String pincode, String paymentMethod) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Secure Payment");
        builder.setMessage("Enter Card Details to Pay ₹" + finalTotalAmount);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText inputCard = new EditText(this);
        inputCard.setHint("Card Number (16 digits)");
        inputCard.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputCard);

        final EditText inputCVV = new EditText(this);
        inputCVV.setHint("CVV (3 digits)");
        inputCVV.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        layout.addView(inputCVV);

        builder.setView(layout);
        builder.setPositiveButton("PAY NOW", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (inputCard.getText().toString().length() < 12) {
                Toast.makeText(CheckoutActivity.this, "Invalid Card Number!", Toast.LENGTH_SHORT).show();
            } else if (inputCVV.getText().toString().length() < 3) {
                Toast.makeText(CheckoutActivity.this, "Invalid CVV!", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                processOrderWithLoading(name, phone, address, city, pincode, paymentMethod, "PAY_" + System.currentTimeMillis());
            }
        });
    }

    private void processOrderWithLoading(String name, String phone, String address, String city, String pincode, String paymentMethod, String paymentId) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(paymentMethod.equals("Online Payment") ? "Processing Payment..." : "Placing order...");
        pd.setCancelable(false);
        pd.show();

        new Handler().postDelayed(() -> {
            pd.dismiss();
            if(paymentMethod.equals("Online Payment")) Toast.makeText(this, "Payment Successful! ✅", Toast.LENGTH_SHORT).show();
            placeOrder(name, phone, address, city, pincode, paymentMethod, paymentId);
        }, 2000);
    }

    private void placeOrder(String name, String phone, String address, String city, String pincode, String paymentMethod, String paymentId) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders");

        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }

        DatabaseReference removeCartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        for (Product product : cartList) {
            String orderId = orderRef.push().getKey();
            HashMap<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", orderId);
            orderMap.put("userId", userId);
            orderMap.put("date", currentDate);
            orderMap.put("productId", product.getId());
            orderMap.put("productName", product.getName());

            try {
                double pPrice = Double.parseDouble(product.getPrice());
                int pQty = Integer.parseInt(product.getQuantity());
                orderMap.put("totalAmount", pPrice * pQty);
            } catch (Exception e) {
                orderMap.put("totalAmount", 0.0);
            }

            orderMap.put("status", "Pending");
            orderMap.put("quantity", product.getQuantity());
            orderMap.put("paymentId", paymentId);
            orderMap.put("paymentMethod", paymentMethod);
            orderMap.put("paymentStatus", paymentMethod.equals("Cash on Delivery") ? "Pending" : "Paid");

            orderMap.put("shippingName", name);
            orderMap.put("shippingPhone", phone);
            orderMap.put("shippingAddress", address);
            orderMap.put("shippingCity", city);
            orderMap.put("shippingPincode", pincode);

            if (orderId != null) orderRef.child(orderId).setValue(orderMap);
        }

        removeCartRef.removeValue().addOnCompleteListener(task -> {
            saveNotificationToDatabase(userId, "Order Placed! 🎉", "Your order has been placed successfully.", "order_placed");
            sendOrderNotification();

            Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
            intent.putExtra("totalAmount", finalTotalAmount);
            intent.putExtra("paymentMethod", paymentMethod);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void saveNotificationToDatabase(String userId, String title, String message, String type) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);
        String notifId = notifRef.push().getKey();
        String timestamp = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());

        HashMap<String, String> notifMap = new HashMap<>();
        notifMap.put("id", notifId);
        notifMap.put("title", title);
        notifMap.put("message", message);
        notifMap.put("type", type);
        notifMap.put("timestamp", timestamp);

        if (notifId != null) notifRef.child(notifId).setValue(notifMap);
    }

    private void sendOrderNotification() {
        String channelId = "order_notifications";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Order Updates", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MyOrdersActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Order Confirmed! 🎉")
                .setContentText("Your checkout was successful.")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
    }
}