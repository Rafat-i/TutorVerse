package com.example.tutorverse;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        gridAdapter = new ScheduleGridAdapter(this);
        gridDays.setAdapter(gridAdapter);

        meetingList = new ArrayList<>();
        meetingAdapter = new ScheduleMeetingAdapter(this, meetingList);
        lvSchedule.setAdapter(meetingAdapter);

        gridDays.setOnItemClickListener((parent, view, position, id) -> {
            String dayName = gridAdapter.getFullDay(position);
            tvSelectedDay.setText("Day: " + dayName);
            loadMeetingsFor(selectedCourse, dayName);
        });
    }

    private void loadMeetingsFor(String course, String dayName) {
        meetingList.clear();

        dbAvailability.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot tutorSnap : snapshot.getChildren()) {

                String tutorName = tutorSnap.child("tutorName").getValue(String.class);
                if (tutorName == null) continue;

                if (!tutorSnap.child("courses").hasChild(course)) continue;
                if (!tutorSnap.child("courses").child(course).hasChild(dayName)) continue;

                DataSnapshot daySnap = tutorSnap.child("courses").child(course).child(dayName);

                String start = daySnap.child("start").getValue(String.class);
                String end = daySnap.child("end").getValue(String.class);

                if (start == null || end == null) continue;

                String timeText = dayName + " " + start + " - " + end;

                meetingList.add(new ScheduleMeeting(tutorName, course, timeText, false));
            }

            meetingAdapter.notifyDataSetChanged();
        });
    }
}
