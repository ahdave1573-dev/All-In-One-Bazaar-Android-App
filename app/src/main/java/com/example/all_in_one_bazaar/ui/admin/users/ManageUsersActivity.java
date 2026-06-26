package com.example.all_in_one_bazaar.ui.admin.users;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.ui.admin.adapter.AdminUserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageUsersActivity extends AppCompatActivity {

    private CardView btnBack;
    private RecyclerView recyclerUsers;
    private ProgressBar progressBar;
    private TextView txtEmptyState, txtUserCount;

    private DatabaseReference usersRef;
    private List<Map<String, String>> userList;
    private AdminUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Init views
        btnBack = findViewById(R.id.btnBack);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        progressBar = findViewById(R.id.progressBar);
        txtEmptyState = findViewById(R.id.txtEmptyState);
        txtUserCount = findViewById(R.id.txtUserCount);

        // Setup
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        userList = new ArrayList<>();

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this, userList);
        recyclerUsers.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        Map<String, String> user = new HashMap<>();
                        user.put("uid", ds.getKey());
                        user.put("name", getStringVal(ds, "name"));
                        user.put("email", getStringVal(ds, "email"));

                        // ફોનની જગ્યાએ પ્રોવાઈડર (Google/Email) ચેક કરીએ છીએ
                        String provider = getStringVal(ds, "provider");
                        if (provider.isEmpty()) {
                            provider = getStringVal(ds, "loginMethod"); // Alternative key
                        }
                        user.put("provider", provider);

                        user.put("role", getStringVal(ds, "role"));
                        user.put("image", getStringVal(ds, "image"));

                        userList.add(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                txtUserCount.setText(userList.size() + " Registered Users");
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (userList.isEmpty()) {
                    txtEmptyState.setVisibility(View.VISIBLE);
                    recyclerUsers.setVisibility(View.GONE);
                } else {
                    txtEmptyState.setVisibility(View.GONE);
                    recyclerUsers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringVal(DataSnapshot ds, String key) {
        Object val = ds.child(key).getValue();
        return val != null ? String.valueOf(val) : "";
    }
}