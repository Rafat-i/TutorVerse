package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CourseSelectionActivity extends AppCompatActivity {

    ListView lvCourses;
    Button btnBack;
    TextView tvCourseTitle, tvCourseInfo;

    ArrayList<String> courses;
    ArrayAdapter<String> adapter;

    DatabaseReference dbAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvCourses = findViewById(R.id.lvCourses);
        btnBack = findViewById(R.id.btnBack);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseInfo = findViewById(R.id.tvCourseInfo);

        dbAvailability = FirebaseDatabase.getInstance().getReference("availability");

        courses = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                courses);
        lvCourses.setAdapter(adapter);

        // Back button -> go back to Profile
        btnBack.setOnClickListener(v -> finish());

        // When user taps a course -> go to ScheduleActivity
        lvCourses.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = courses.get(position);
            Intent i = new Intent(CourseSelectionActivity.this, ScheduleActivity.class);
            i.putExtra("courseName", selectedCourse);
            startActivity(i);
        });

        loadCoursesFromFirebase();
    }

    private void loadCoursesFromFirebase() {
        dbAvailability.get().addOnSuccessListener(snapshot -> {

            Set<String> courseSet = new HashSet<>();

            // Loop over all tutors
            for (DataSnapshot tutorSnap : snapshot.getChildren()) {
                DataSnapshot coursesSnap = tutorSnap.child("courses");
                for (DataSnapshot courseSnap : coursesSnap.getChildren()) {
                    String courseName = courseSnap.getKey();
                    if (courseName != null) {
                        courseSet.add(courseName);
                    }
                }
            }

            courses.clear();
            courses.addAll(courseSet);
            adapter.notifyDataSetChanged();

            if (courses.isEmpty()) {
                tvCourseInfo.setText("No courses have availability yet.");
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading courses: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
