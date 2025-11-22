package com.example.tutorverse;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class TeacherAvailabilityActivity extends AppCompatActivity {

    Spinner spCourse, spDay;
    EditText edStartTime, edEndTime;
    Button btnSaveAvailability, btnBack;

    FirebaseAuth auth;
    DatabaseReference dbUsers;
    DatabaseReference dbAvailability;
    DatabaseReference dbCourses;

    String uid;
    String tutorName = "";

    ArrayList<String> courseList;
    ArrayAdapter<String> courseAdapter;

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
        edStartTime = findViewById(R.id.edStartTime);
        edEndTime = findViewById(R.id.edEndTime);
        btnSaveAvailability = findViewById(R.id.btnSaveAvailability);
        btnBack = findViewById(R.id.btnBack);

        auth = FirebaseAuth.getInstance();
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

    private void saveAvailability() {
        if (courseList.isEmpty()) {
            Toast.makeText(this, "No course selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        String course = spCourse.getSelectedItem().toString();
        String day = spDay.getSelectedItem().toString();
        String start = edStartTime.getText().toString().trim();
        String end = edEndTime.getText().toString().trim();

        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Please enter start and end time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!tutorName.isEmpty()) {
            dbAvailability.child("tutorName").setValue(tutorName);
        }

        DatabaseReference dayRef = dbAvailability
                .child("courses")
                .child(course)
                .child(day);

        dayRef.child("start").setValue(start);
        dayRef.child("end").setValue(end)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Availability saved", Toast.LENGTH_SHORT).show();
                    edStartTime.setText("");
                    edEndTime.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
