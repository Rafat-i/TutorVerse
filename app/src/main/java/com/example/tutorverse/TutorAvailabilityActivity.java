package com.example.tutorverse;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TutorAvailabilityActivity extends AppCompatActivity {

    Spinner spCourse, spDay, spStartTime, spEndTime;
    Button btnSaveAvailability, btnBack;
    LinearLayout layoutDayPreview;

    FirebaseAuth auth;
    DatabaseReference dbUsers;
    DatabaseReference dbAvailability;
    DatabaseReference dbCourses;

    String uid;
    String tutorName = "";

    ArrayList<String> courseList;
    ArrayAdapter<String> courseAdapter;

    String[] allTimes = {
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_availability);

        // --- UI PADDING FIX ---
        // Converts 24dp to pixels to ensure consistent spacing on all screens
        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // We ADD the system bar size to our desired 24dp padding
            v.setPadding(
                    systemBars.left + paddingPx,
                    systemBars.top + paddingPx,
                    systemBars.right + paddingPx,
                    systemBars.bottom + paddingPx
            );
            return insets;
        });
        // ----------------------

        spCourse = findViewById(R.id.spCourse);
        spDay = findViewById(R.id.spDay);
        spStartTime = findViewById(R.id.spStartTime);
        spEndTime = findViewById(R.id.spEndTime);
        btnSaveAvailability = findViewById(R.id.btnSaveAvailability);
        btnBack = findViewById(R.id.btnBack);
        layoutDayPreview = findViewById(R.id.layoutDayPreview);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }
        uid = auth.getCurrentUser().getUid();

        dbUsers = FirebaseDatabase.getInstance().getReference("users").child(uid);
        dbAvailability = FirebaseDatabase.getInstance().getReference("availability").child(uid);
        dbCourses = FirebaseDatabase.getInstance().getReference("course");

        dbUsers.child("username").get().addOnSuccessListener(snapshot -> {
            String name = snapshot.getValue(String.class);
            if (name != null) tutorName = name;
        });

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        ArrayAdapter<String> dayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        // Listener to update preview when day changes
        spDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateDayPreview(days[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<String> timeAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allTimes);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStartTime.setAdapter(timeAdapter);
        spEndTime.setAdapter(timeAdapter);

        courseList = new ArrayList<>();
        courseAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCourse.setAdapter(courseAdapter);

        loadCoursesFromFirebase();

        btnSaveAvailability.setOnClickListener(v -> checkConflictAndSave());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCoursesFromFirebase() {
        dbCourses.get().addOnSuccessListener(snapshot -> {
            courseList.clear();

            for (DataSnapshot courseSnap : snapshot.getChildren()) {
                String courseName = courseSnap.getKey();
                if (courseName != null && !courseName.trim().isEmpty()) {
                    courseList.add(courseName);
                }
            }

            courseAdapter.notifyDataSetChanged();

            if (courseList.isEmpty()) {
                Toast.makeText(this, "No courses found. Please add some in Firebase.", Toast.LENGTH_SHORT).show();
            }
            // Removed the manual updateDayPreview call here to prevent double loading.
            // The spinner listener above handles the initial load automatically.

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading courses: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void updateDayPreview(String day) {
        dbAvailability.child("courses").get().addOnSuccessListener(snapshot -> {
            // --- FIX: Clear views INSIDE the success listener ---
            layoutDayPreview.removeAllViews();

            for (String time : allTimes) {
                String foundCourse = null;

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String courseName = courseSnap.getKey();
                    if (courseSnap.child(day).hasChild(time)) {
                        foundCourse = courseName;
                        break;
                    }
                }

                // Create a horizontal row for the visual preview
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);
                row.setGravity(Gravity.CENTER_VERTICAL);

                TextView tvTime = new TextView(this);
                tvTime.setText(time);
                tvTime.setWidth(150);
                tvTime.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvCourse = new TextView(this);
                tvCourse.setPadding(16, 8, 16, 8);

                if (foundCourse != null) {
                    tvCourse.setText(foundCourse);
                    tvCourse.setBackgroundColor(getColorForCourse(foundCourse));
                    tvCourse.setTextColor(0xFFFFFFFF); // White text
                } else {
                    tvCourse.setText("Available");
                    tvCourse.setBackgroundColor(0xFFE0E0E0); // Light Grey
                    tvCourse.setTextColor(0xFF757575); // Dark Grey text
                }

                row.addView(tvTime);
                row.addView(tvCourse);
                layoutDayPreview.addView(row);
            }
        });
    }

    private int getColorForCourse(String course) {
        String lower = course.toLowerCase();
        if (lower.contains("android")) return 0xFF81C784;
        else if (lower.contains("java")) return 0xFF64B5F6;
        else if (lower.contains("web")) return 0xFFFFB74D;
        else if (lower.contains("data")) return 0xFFBA68C8;
        else if (lower.contains("oop")) return 0xFF4DB6AC;
        else return 0xFFA1887F;
    }

    private int indexOfTime(String time) {
        for (int i = 0; i < allTimes.length; i++) {
            if (allTimes[i].equals(time)) return i;
        }
        return -1;
    }

    private void checkConflictAndSave() {
        if (courseList.isEmpty()) {
            Toast.makeText(this, "No course selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        String course = spCourse.getSelectedItem().toString();
        String day = spDay.getSelectedItem().toString();
        String start = spStartTime.getSelectedItem().toString();
        String end = spEndTime.getSelectedItem().toString();

        int startIndex = indexOfTime(start);
        int endIndex = indexOfTime(end);

        if (startIndex == -1 || endIndex == -1) {
            Toast.makeText(this, "Invalid time selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startIndex >= endIndex) {
            Toast.makeText(this, "End time must be after start time.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbAvailability.child("courses").get().addOnSuccessListener(snapshot -> {
            String conflictingCourse = null;

            for (int i = startIndex; i < endIndex; i++) {
                String timeSlot = allTimes[i];
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String existingCourseName = courseSnap.getKey();
                    if (existingCourseName == null || existingCourseName.equals(course)) continue;

                    if (courseSnap.child(day).hasChild(timeSlot)) {
                        conflictingCourse = existingCourseName;
                        break;
                    }
                }
                if (conflictingCourse != null) break;
            }

            if (conflictingCourse != null) {
                showConflictDialog(conflictingCourse, startIndex, endIndex, course, day, snapshot);
            } else {
                writeAvailability(startIndex, endIndex, course, day);
            }

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error checking availability.", Toast.LENGTH_SHORT).show()
        );
    }

    private void showConflictDialog(String conflictingCourse, int startIndex, int endIndex, String newCourse, String day, DataSnapshot allCoursesSnap) {
        new AlertDialog.Builder(this)
                .setTitle("Conflict Detected")
                .setMessage("There is already a class for " + conflictingCourse + " during this time. Would you like to replace it?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    overwriteAvailability(startIndex, endIndex, newCourse, day, allCoursesSnap);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void overwriteAvailability(int startIndex, int endIndex, String newCourse, String day, DataSnapshot allCoursesSnap) {
        for (int i = startIndex; i < endIndex; i++) {
            String timeSlot = allTimes[i];
            for (DataSnapshot courseSnap : allCoursesSnap.getChildren()) {
                String existingCourseName = courseSnap.getKey();
                if (existingCourseName != null && !existingCourseName.equals(newCourse)) {
                    if (courseSnap.child(day).hasChild(timeSlot)) {
                        courseSnap.getRef().child(day).child(timeSlot).removeValue();
                    }
                }
            }
        }
        writeAvailability(startIndex, endIndex, newCourse, day);
    }

    private void writeAvailability(int startIndex, int endIndex, String course, String day) {
        if (!tutorName.isEmpty()) {
            dbAvailability.child("tutorName").setValue(tutorName);
        }

        DatabaseReference dayRef = dbAvailability.child("courses")
                .child(course)
                .child(day);

        for (int i = startIndex; i < endIndex; i++) {
            String slot = allTimes[i];
            dayRef.child(slot).setValue(true);
        }

        Toast.makeText(this, "Availability saved.", Toast.LENGTH_SHORT).show();
        updateDayPreview(day);
    }
}