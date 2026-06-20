package com.example.all_in_one_bazaar.ui.admin.dashboard;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.order.AdminOrdersActivity;
import com.example.all_in_one_bazaar.ui.admin.reports.AdminReviewsActivity;
import com.example.all_in_one_bazaar.ui.admin.category.ManageCategoriesActivity;
import com.example.all_in_one_bazaar.ui.admin.product.ManageProductsActivity;
import com.example.all_in_one_bazaar.ui.admin.users.ManageUsersActivity;
import com.example.all_in_one_bazaar.ui.admin.reports.SalesReportsActivity;
import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {

    // cardReviews નવું ઉમેર્યું છે
    private MaterialCardView cardAddProduct, cardOrders, cardUsers, cardCategories, cardReports, cardReviews, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // ૧. Bind Views (ID સાથે જોડાણ)
        initViews();

        // ૨. Click Listeners સેટ કરવા
        setupClickListeners();
    }

    private void initViews() {
        cardAddProduct = findViewById(R.id.cardAddProduct);
        cardOrders     = findViewById(R.id.cardOrders);
        cardUsers      = findViewById(R.id.cardUsers);
        cardCategories = findViewById(R.id.cardCategories);
        cardReports    = findViewById(R.id.cardReports);
        cardReviews    = findViewById(R.id.cardReviews); // નવું ID જોડાણ
        cardLogout     = findViewById(R.id.cardLogout);
    }

    private void setupClickListeners() {
        // ૧. Manage Products Page
        cardAddProduct.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ManageProductsActivity.class)));

        // ૨. View Orders Page
        cardOrders.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminOrdersActivity.class)));

        // ૩. View Users Page
        cardUsers.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class)));

        // ૪. Categories Page
        cardCategories.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, ManageCategoriesActivity.class)));

        // ૫. Reports Page
        cardReports.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, SalesReportsActivity.class)));

        // ૬. Ratings / Reviews Page (નવી ક્લિક ઇવેન્ટ)
        cardReviews.setOnClickListener(v ->
                startActivity(new Intent(AdminDashboardActivity.this, AdminReviewsActivity.class)));

        // ૭. Logout
        cardLogout.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, com.example.all_in_one_bazaar.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}