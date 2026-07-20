package com.example.all_in_one_bazaar.ui.admin.reports;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.adapter.AdminReviewAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private CardView btnBack;

    private AdminReviewAdapter adapter;
    private List<Map<String, Object>> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        // 🛠️ Status Bar Fix (એડમિન થીમ પ્રમાણે ડાર્ક ક્રિમસન કલર સેટ કર્યો)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.view.Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.parseColor("#C70039")); // બદલેલો કલર

            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(0); // White status bar icons
            }
        }

        // Init views
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerAdminReviews);
        progressBar = findViewById(R.id.progressBarReviews);
        layoutEmpty = findViewById(R.id.layoutEmptyReviews);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        adapter = new AdminReviewAdapter(this, reviewList);
        recyclerView.setAdapter(adapter);

        fetchAllReviews();
    }

    private void fetchAllReviews() {
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews");
        progressBar.setVisibility(View.VISIBLE);

        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot reviewSnapshot : productSnapshot.getChildren()) {

                        String rating = String.valueOf(reviewSnapshot.child("rating").getValue());
                        String comment = String.valueOf(reviewSnapshot.child("comment").getValue());
                        String userId = String.valueOf(reviewSnapshot.child("userId").getValue());

                        Map<String, Object> map = new HashMap<>();
                        map.put("rating", rating);
                        map.put("comment", comment);
                        map.put("userId", userId);
                        map.put("userName", "Loading..."); // પહેલા લોડીંગ બતાવશે

                        reviewList.add(map);

                        // યુઝરનું નામ લાવવા માટેનું ફંક્શન
                        fetchUserName(userId, map);
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (reviewList.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminReviewsActivity.this, "Database Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // યુઝરના નોડમાંથી નામ લાવવા માટેની મેથડ
    private void fetchUserName(String userId, Map<String, Object> map) {
        if (userId == null || userId.equals("null") || userId.isEmpty()) {
            map.put("userName", "Unknown User");
            adapter.notifyDataSetChanged();
            return;
        }

        // નોંધ: જો તમારા ડેટાબેઝમાં યુઝરના નોડનું નામ "users" (સ્મોલ) હોય તો નીચે "Users" ની જગ્યાએ "users" કરી દેજો.
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // જો નામ "name" તરીકે સેવ કર્યું હોય (જો "fullName" હોય તો તે અહી મૂકો)
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        map.put("userName", name);
                    } else {
                        map.put("userName", "Unknown User");
                    }
                } else {
                    map.put("userName", "Unknown User");
                }
                adapter.notifyDataSetChanged(); // નામ મળે એટલે લિસ્ટ રિફ્રેશ થશે
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // એરર આવે તો કઈ નહિ
            }
        });
    }
}