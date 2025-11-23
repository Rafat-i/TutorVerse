package com.example.tutorverse;

import android.os.Bundle;
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
    DatabaseReference dbAvailabilityCourses;

    List<String> timeStarts;
    List<String> timeLabels;
    Map<String, String> slotCourseMap;

    WeeklyScheduleAdapter adapter;

    String tutorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_calendar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

        dbAvailabilityCourses = FirebaseDatabase.getInstance()
                .getReference("availability")
                .child(tutorUid)
                .child("courses");

        String[] fullTimes = {
                "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00",
                "16:00", "17:00", "18:00"
        };

        timeStarts = new ArrayList<>();
        timeLabels = new ArrayList<>();

        for (int i = 0; i < fullTimes.length - 1; i++) {
            timeStarts.add(fullTimes[i]);
            String label = fullTimes[i] + " - " + fullTimes[i + 1];
            timeLabels.add(label);
        }

        slotCourseMap = new HashMap<>();

        adapter = new WeeklyScheduleAdapter(this, timeStarts, timeLabels, slotCourseMap);
        lvWeekly.setAdapter(adapter);

        loadAvailability();
    }

    private void loadAvailability() {
        dbAvailabilityCourses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                slotCourseMap.clear();

                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String courseName = courseSnap.getKey();
                    if (courseName == null) continue;

                    for (DataSnapshot daySnap : courseSnap.getChildren()) {
                        String dayName = daySnap.getKey();
                        if (dayName == null) continue;

                        for (DataSnapshot slotSnap : daySnap.getChildren()) {
                            String timeKey = slotSnap.getKey();
                            if (timeKey == null) continue;

                            String mapKey = dayName + "|" + timeKey;
                            slotCourseMap.put(mapKey, courseName);
                        }
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TutorCalendarActivity.this,
                        "Error loading availability", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
