package com.example.all_in_one_bazaar.ui.client.features; // 🛠️ FIX: પેકેજ નામ એકદમ સ્મોલ અક્ષરોમાં ફિક્સ કર્યું

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Notification;
import com.example.all_in_one_bazaar.ui.client.adapter.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerNotifications;
    ProgressBar progressBar;
    LinearLayout layoutEmpty;
    CardView btnBack, btnClearAll;

    NotificationAdapter adapter;
    List<Notification> list;
    DatabaseReference notificationRef;
    String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // 🛠️ Status Bar Color & Light Text Fix
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

        // Views Initialization
        btnBack = findViewById(R.id.btnBack);
        btnClearAll = findViewById(R.id.btnClearAll);
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            notificationRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentUid);
        }

        btnBack.setOnClickListener(v -> finish());

        // બધી જ નોટિફિકેશન એકસાથે ડીલીટ કરવા માટે (Clear All)
        if (btnClearAll != null) {
            btnClearAll.setOnClickListener(v -> {
                if (list == null || list.isEmpty()) {
                    Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show();
                    return;
                }

                // યુઝરને કન્ફર્મેશન પૂછવા માટે ડાયલોગ
                new AlertDialog.Builder(this)
                        .setTitle("Clear All Notifications")
                        .setMessage("Are you sure you want to delete all notifications?")
                        .setPositiveButton("Yes, Clear All", (dialog, which) -> clearAllNotifications())
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create().show();
            });
        }

        // RecyclerView Setup
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();

        // એડેપ્ટરની અંદર સિંગલ આઇટમ લોન્ગ પ્રેસથી ડિલીટ કરવા માટેની ઇવેન્ટ પાસ કરી
        adapter = new NotificationAdapter(this, list, notificationModel -> {
            // સિંગલ આઇટમ ડિલીટ કરવા માટે કન્ફર્મેશન ડાયલોગ
            new AlertDialog.Builder(this)
                    .setTitle("Delete Notification")
                    .setMessage("Do you want to delete this notification?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteSingleNotification(notificationModel.getId()))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        });

        recyclerNotifications.setAdapter(adapter);

        if (currentUid != null) {
            loadNotifications();
        } else {
            progressBar.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void loadNotifications() {
        if (notificationRef == null) return;

        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Notification model = ds.getValue(Notification.class);
                    if (model != null) {
                        model.setId(ds.getKey());
                        list.add(model);
                    }
                }

                Collections.reverse(list);
                adapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                if (list.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    // 🛠️ FIX: સિંગલ નોટિફિકેશન ડીલીટ કરવાનું ફંક્શન (ત્યાંથી એરર વાળી લાઈન કાઢી નાખી છે)
    private void deleteSingleNotification(String notificationId) {
        if (notificationRef != null && notificationId != null) {
            progressBar.setVisibility(View.VISIBLE);
            notificationRef.child(notificationId).removeValue()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(NotificationActivity.this, "Notification deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NotificationActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // બધી જ નોટિફિકેશન એકસાથે ક્લીન કરવાનું ફંક્શન
    private void clearAllNotifications() {
        if (notificationRef != null) {
            progressBar.setVisibility(View.VISIBLE);
            notificationRef.removeValue().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(NotificationActivity.this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NotificationActivity.this, "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}