package com.example.tutorverse;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorCalendarActivity extends AppCompatActivity {

    ListView lvWeekly;
    Button btnBackCalendar;

    FirebaseAuth auth;
    DatabaseReference dbAvailability;
    DatabaseReference dbBookings;

    List<String> timeStarts;
    List<String> timeLabels;

    Map<String, WeeklyScheduleAdapter.SlotInfo> slotMap;

    WeeklyScheduleAdapter adapter;
    String tutorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_calendar);

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

        lvWeekly = findViewById(R.id.lvWeekly);
        btnBackCalendar = findViewById(R.id.btnBackCalendar);

        btnBackCalendar.setOnClickListener(v -> finish());

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        tutorUid = auth.getCurrentUser().getUid();

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

        loadData();
    }

    private void loadData() {
        // 1. Load Availability first
        dbAvailability.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot availSnap) {
                slotMap.clear();

                // Fill map with "Available" slots
                for (DataSnapshot courseSnap : availSnap.getChildren()) {
                    String courseName = courseSnap.getKey();
                    for (DataSnapshot daySnap : courseSnap.getChildren()) {
                        String day = daySnap.getKey();
                        for (DataSnapshot slotSnap : daySnap.getChildren()) {
                            String time = slotSnap.getKey();
                            String key = day + "|" + time;

                            // Create SlotInfo (Initially no student)
                            slotMap.put(key, new WeeklyScheduleAdapter.SlotInfo(courseName, null, null, null));
                        }
                    }
                }

                // 2. Load Bookings to overlay student info
                dbBookings.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot bookingSnap) {
                        for (DataSnapshot slotBooking : bookingSnap.getChildren()) {
                            String key = slotBooking.getKey();

                            if (slotMap.containsKey(key)) {
                                String studentName = slotBooking.child("studentName").getValue(String.class);
                                String studentUid = slotBooking.child("studentUid").getValue(String.class);
                                String bookingId = slotBooking.getKey(); // simplistic booking key

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
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TutorCalendarActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}