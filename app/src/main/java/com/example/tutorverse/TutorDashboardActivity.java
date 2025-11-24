package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

public class TutorDashboardActivity extends AppCompatActivity {

    Button btnSetAvailability, btnViewBookings, btnEditProfile, btnBack, btnInbox;
    TextView tvUnreadBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_dashboard);

        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + paddingPx,
                    systemBars.top + paddingPx,
                    systemBars.right + paddingPx,
                    systemBars.bottom + paddingPx
            );
            return insets;
        });

        btnSetAvailability = findViewById(R.id.btnSetAvailability);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBack = findViewById(R.id.btnBack);
        btnInbox = findViewById(R.id.btnInbox);

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnSetAvailability.setOnClickListener(v ->
                startActivity(new Intent(this, TutorAvailabilityActivity.class)));

        btnViewBookings.setOnClickListener(v ->
                startActivity(new Intent(this, TutorCalendarActivity.class)));

        btnInbox.setOnClickListener(v ->
                startActivity(new Intent(this, InboxActivity.class)));

        btnBack.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
    private void setupBadgeListener() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbChats = FirebaseDatabase.getInstance().getReference("user-chats").child(uid);

        dbChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalUnread = 0;
                for (DataSnapshot chat : snapshot.getChildren()) {
                    Integer count = chat.child("unreadCount").getValue(Integer.class);
                    if (count != null) {
                        totalUnread += count;
                    }
                }

                if (totalUnread > 0) {
                    tvUnreadBadge.setText(String.valueOf(totalUnread));
                    tvUnreadBadge.setVisibility(View.VISIBLE);
                } else {
                    tvUnreadBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}