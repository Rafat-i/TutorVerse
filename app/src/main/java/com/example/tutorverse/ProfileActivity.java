package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    TextView tvUsername, tvEmail, tvRole;
    EditText edNewUsername, edBio;
    Button btnUpdateProfile, btnLogout;

    LinearLayout btnDashboard, btnInbox, btnEditProfile;
    FirebaseAuth auth;
    DatabaseReference db;

    TextView tvUnreadBadge;
    String myUid;
    String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initialize();
        setupNavbar();
        setupUnreadbadge();
        loadUserData();
    }

    private void initialize() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);

        edNewUsername = findViewById(R.id.edNewUsername);
        edBio = findViewById(R.id.edBio);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("users");

        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void setupNavbar() {
        btnDashboard = findViewById(R.id.btnDashBoard);
        btnInbox = findViewById(R.id.btnInbox);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        tvUnreadBadge = findViewById(R.id.tvUnreadBadge);

        // Check user role and navigate to correct dashboard
        btnDashboard.setOnClickListener(v -> {
            DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("users");
            dbUsers.child(myUid).child("role").get().addOnSuccessListener(snapshot -> {
                String role = snapshot.getValue(String.class);
                Intent intent;

                if (role != null && role.trim().equalsIgnoreCase("Tutor")) {
                    intent = new Intent(this, TutorDashboardActivity.class);
                } else {
                    intent = new Intent(this, StudentDashboardActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });
        });

        btnInbox.setOnClickListener(v -> startActivity(new Intent(this, InboxActivity.class)));

        btnEditProfile.setOnClickListener(v -> Toast.makeText(this, "Already in Profile", Toast.LENGTH_SHORT).show());
    }

    private void setupUnreadbadge() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbChats = FirebaseDatabase.getInstance().getReference("user-chats").child(uid);

        dbChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread = 0;
                for (DataSnapshot chat : snapshot.getChildren()) {
                    Integer count = chat.child("unreadCount").getValue(Integer.class);
                    if (count != null)
                        unread += count;
                }

                if (unread > 0) {
                    tvUnreadBadge.setText(String.valueOf(unread));
                    tvUnreadBadge.setVisibility(TextView.VISIBLE);
                } else {
                    tvUnreadBadge.setVisibility(TextView.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.child(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String username = snapshot.child("username").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);
                String bio = snapshot.child("bio").getValue(String.class);

                userRole = role;

                tvUsername.setText(username);
                tvEmail.setText(email);
                tvRole.setText(role);

                if (bio != null) {
                    edBio.setText(bio);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateProfile() {
        String newUsername = edNewUsername.getText().toString().trim();
        String newBio = edBio.getText().toString().trim();

        if (newUsername.isEmpty() && newBio.isEmpty()) {
            Toast.makeText(this, "Nothing to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();

        if (!newUsername.isEmpty()) {
            updates.put("username", newUsername);
        }
        if (!newBio.isEmpty()) {
            updates.put("bio", newBio);
        }

        db.child(uid).updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    if (!newUsername.isEmpty()) {
                        tvUsername.setText(newUsername);
                        edNewUsername.setText("");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}