package com.example.tutorverse;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import models.ScheduleMeeting;

public class ScheduleActivity extends AppCompatActivity {

    TextView tvSelectedCourse, tvSelectedDay;
    TextView tvDaySectionHeader, tvTutorSectionHeader;
    LinearLayout layoutDayContent, layoutTutorContent;
    GridView gridDays;
    ListView lvSchedule;
    ScheduleGridAdapter gridAdapter;
    ArrayList<ScheduleMeeting> meetingList;
    ScheduleMeetingAdapter meetingAdapter;

    DatabaseReference dbAvailability;
    DatabaseReference dbBookings;
    FirebaseAuth auth;

    String selectedCourse = "";
    Button btnBackSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        btnBackSchedule = findViewById(R.id.btnBackSchedule);
        btnBackSchedule.setOnClickListener(v -> finish());

        tvSelectedCourse = findViewById(R.id.tvSelectedCourse);
        tvSelectedDay = findViewById(R.id.tvSelectedDay);
        gridDays = findViewById(R.id.gridDays);
        lvSchedule = findViewById(R.id.lvSchedule);
        tvDaySectionHeader = findViewById(R.id.tvDaySectionHeader);
        tvTutorSectionHeader = findViewById(R.id.tvTutorSectionHeader);
        layoutDayContent = findViewById(R.id.layoutDayContent);
        layoutTutorContent = findViewById(R.id.layoutTutorContent);

        // Toggle Sections
        tvDaySectionHeader.setOnClickListener(v -> {
            if (layoutDayContent.getVisibility() == View.VISIBLE) {
                layoutDayContent.setVisibility(View.GONE);
                tvDaySectionHeader.setText("► Select a day (Mon–Fri)");
            } else {
                layoutDayContent.setVisibility(View.VISIBLE);
                tvDaySectionHeader.setText("▼ Select a day (Mon–Fri)");
            }
        });

        tvTutorSectionHeader.setOnClickListener(v -> {
            if (layoutTutorContent.getVisibility() == View.VISIBLE) {
                layoutTutorContent.setVisibility(View.GONE);
                tvTutorSectionHeader.setText("► Available tutors");
            } else {
                layoutTutorContent.setVisibility(View.VISIBLE);
                tvTutorSectionHeader.setText("▼ Available tutors");
            }
        });

        selectedCourse = getIntent().getStringExtra("courseName");
        if (selectedCourse == null) selectedCourse = "";
        tvSelectedCourse.setText("Course: " + selectedCourse);

        dbAvailability = FirebaseDatabase.getInstance().getReference("availability");
        dbBookings = FirebaseDatabase.getInstance().getReference("bookings");

        gridAdapter = new ScheduleGridAdapter(this);
        gridDays.setAdapter(gridAdapter);

        meetingList = new ArrayList<>();
        meetingAdapter = new ScheduleMeetingAdapter(this, meetingList);
        lvSchedule.setAdapter(meetingAdapter);

        gridDays.setOnItemClickListener((parent, view, position, id) -> {
            // Highlighting Logic
            gridAdapter.setSelectedPosition(position);
            String dayName = gridAdapter.getFullDay(position);
            tvSelectedDay.setText("Day: " + dayName);
            loadMeetingsFor(selectedCourse, dayName);
        });
    }

    private void loadMeetingsFor(String course, String dayName) {
        meetingList.clear();
        meetingAdapter.notifyDataSetChanged();

        // 1. Get all availability
        dbAvailability.get().addOnSuccessListener(availSnapshot -> {

            // 2. Get all existing bookings to check conflicts
            dbBookings.get().addOnSuccessListener(bookingSnapshot -> {

                for (DataSnapshot tutorSnap : availSnapshot.getChildren()) {
                    String tutorUID = tutorSnap.getKey();
                    String tutorName = tutorSnap.child("tutorName").getValue(String.class);
                    if (tutorName == null) tutorName = "Unknown Tutor";

                    // Drill down: courses -> CourseName -> DayName
                    if (!tutorSnap.child("courses").child(course).child(dayName).exists()) continue;

                    DataSnapshot slotsSnap = tutorSnap.child("courses").child(course).child(dayName);

                    // Iterate through every time slot (e.g., "08:00")
                    for (DataSnapshot timeSnap : slotsSnap.getChildren()) {
                        String startTime = timeSnap.getKey(); // "08:00"
                        if (startTime == null) continue;

                        // Check if this slot is already booked in /bookings/{tutorUID}/{day|time}
                        String bookingKey = dayName + "|" + startTime;
                        boolean isTaken = false;
                        String takenBy = "";

                        if (bookingSnapshot.child(tutorUID).hasChild(bookingKey)) {
                            isTaken = true;
                            takenBy = bookingSnapshot.child(tutorUID).child(bookingKey).child("studentName").getValue(String.class);
                        }

                        // Create meeting object
                        ScheduleMeeting meeting = new ScheduleMeeting(
                                tutorName,
                                course,
                                dayName + " " + startTime, // Display String
                                isTaken
                        );

                        // Store hidden metadata for booking logic
                        meeting.setTutorUid(tutorUID);
                        meeting.setDay(dayName);
                        meeting.setStartTime(startTime);
                        meeting.setTakenBy(takenBy); // To check if "I" booked it

                        meetingList.add(meeting);
                    }
                }

                if (meetingList.isEmpty()) {
                    Toast.makeText(this, "No tutors available for " + dayName, Toast.LENGTH_SHORT).show();
                }

                meetingAdapter.notifyDataSetChanged();
            });
        });
    }
}