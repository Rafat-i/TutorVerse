package com.example.tutorverse;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class TutorAvailabilityActivity extends AppCompatActivity {

    Spinner spCourse, spDay, spStartTime, spEndTime;
    Button btnSaveAvailability, btnBack;

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spCourse = findViewById(R.id.spCourse);
        spDay = findViewById(R.id.spDay);
        spStartTime = findViewById(R.id.spStartTime);
        spEndTime = findViewById(R.id.spEndTime);
        btnSaveAvailability = findViewById(R.id.btnSaveAvailability);
        btnBack = findViewById(R.id.btnBack);

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

        btnSaveAvailability.setOnClickListener(v -> saveAvailability());
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

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading courses: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private int indexOfTime(String time) {
        for (int i = 0; i < allTimes.length; i++) {
            if (allTimes[i].equals(time)) return i;
        }
        return -1;
    }

    private void saveAvailability() {
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

        DatabaseReference dayRef = dbAvailability.child("courses")
                .child(course)
                .child(day);

        dayRef.get().addOnSuccessListener(snapshot -> {
            boolean conflict = false;

            for (int i = startIndex; i < endIndex; i++) {
                String slot = allTimes[i];
                if (snapshot.hasChild(slot)) {
                    conflict = true;
                    break;
                }
            }

            if (conflict) {
                Toast.makeText(this,
                        "You already have availability for this course at that time.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (!tutorName.isEmpty()) {
                dbAvailability.child("tutorName").setValue(tutorName);
            }

            for (int i = startIndex; i < endIndex; i++) {
                String slot = allTimes[i];
                dayRef.child(slot).setValue(true);
            }

            Toast.makeText(this, "Availability saved", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error checking availability.", Toast.LENGTH_SHORT).show()
        );
    }
}
