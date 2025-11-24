package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class StudentDashboardActivity extends AppCompatActivity {

    Button btnEditProfile, btnBookMeeting, btnViewBookings, btnBack, btnInbox;
    TextView tvUnreadBadge; // New

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);

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

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBookMeeting = findViewById(R.id.btnBookMeeting);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnBack = findViewById(R.id.btnBack);
        btnInbox = findViewById(R.id.btnInbox);
        tvUnreadBadge = findViewById(R.id.tvUnreadBadge); // Init Badge

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnBookMeeting.setOnClickListener(v ->
                startActivity(new Intent(this, CourseSelectionActivity.class)));

        btnViewBookings.setOnClickListener(v ->
                startActivity(new Intent(this, StudentBookingsActivity.class)));

        btnInbox.setOnClickListener(v ->
                startActivity(new Intent(this, InboxActivity.class)));

        btnBack.setOnClickListener(v -> finish());

        setupBadgeListener();
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