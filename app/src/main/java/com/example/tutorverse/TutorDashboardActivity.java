package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorDashboardActivity extends AppCompatActivity {

    FloatingActionButton btnSetAvailability;
    View btnInbox, btnEditProfile, btnDashBoard;
    TextView tvUnreadBadge;
    ListView lvWeekly;

    FirebaseAuth auth;
    DatabaseReference dbAvailability;
    DatabaseReference dbBookings;
    String tutorUid;

    List<String> timeStarts;
    List<String> timeLabels;
    Map<String, WeeklyScheduleAdapter.SlotInfo> slotMap;
    WeeklyScheduleAdapter adapter;

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
                    systemBars.bottom
            );
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        tutorUid = auth.getCurrentUser().getUid();

        btnSetAvailability = findViewById(R.id.btnSetAvailability);
        btnInbox = findViewById(R.id.btnInbox);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDashBoard = findViewById(R.id.btnDashBoard);
        tvUnreadBadge = findViewById(R.id.tvUnreadBadge);
        lvWeekly = findViewById(R.id.lvWeekly);

        btnSetAvailability.setOnClickListener(v ->
                startActivity(new Intent(this, TutorAvailabilityActivity.class)));

        btnInbox.setOnClickListener(v ->
                startActivity(new Intent(this, InboxActivity.class)));

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Handle Dashboard button - stay on this activity or show toast
        btnDashBoard.setOnClickListener(v -> {
            Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show();
        });

        setupSchedule();
        setupBadgeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScheduleData();
    }

    private void setupSchedule() {
        dbAvailability = FirebaseDatabase.getInstance().getReference("availability").child(tutorUid).child("courses");
        dbBookings = FirebaseDatabase.getInstance().getReference("bookings").child(tutorUid);

        String[] fullTimes = {
                "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00",
                "16:00", "17:00", "18:00"
        };

        timeStarts = new ArrayList<>();
        timeLabels = new ArrayList<>();

        for (int i = 0; i < fullTimes.length - 1; i++) {
            timeStarts.add(fullTimes[i]);
            timeLabels.add(fullTimes[i] + "\n" + fullTimes[i + 1]);
        }

        slotMap = new HashMap<>();
        adapter = new WeeklyScheduleAdapter(this, timeStarts, timeLabels, slotMap);
        lvWeekly.setAdapter(adapter);
    }

    private void loadScheduleData() {
        dbAvailability.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot availSnap) {
                slotMap.clear();

                for (DataSnapshot courseSnap : availSnap.getChildren()) {
                    String courseName = courseSnap.getKey();
                    for (DataSnapshot daySnap : courseSnap.getChildren()) {
                        String day = daySnap.getKey();
                        for (DataSnapshot slotSnap : daySnap.getChildren()) {
                            String time = slotSnap.getKey();
                            String key = day + "|" + time;

                            slotMap.put(key, new WeeklyScheduleAdapter.SlotInfo(courseName, null, null, null));
                        }
                    }
                }

                dbBookings.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot bookingSnap) {
                        for (DataSnapshot slotBooking : bookingSnap.getChildren()) {
                            String key = slotBooking.getKey();

                            if (slotMap.containsKey(key)) {
                                String studentName = slotBooking.child("studentName").getValue(String.class);
                                String studentUid = slotBooking.child("studentUid").getValue(String.class);
                                String bookingId = slotBooking.getKey();

                                WeeklyScheduleAdapter.SlotInfo info = slotMap.get(key);
                                info.studentName = studentName;
                                info.studentUid = studentUid;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void setupBadgeListener() {
        DatabaseReference dbChats = FirebaseDatabase.getInstance().getReference("user-chats").child(tutorUid);

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