package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
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
    CoursePillAdapter adapter; // CHANGED

    DatabaseReference dbAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_selection);

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

        lvCourses = findViewById(R.id.lvCourses);
        btnBack = findViewById(R.id.btnBack);
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseInfo = findViewById(R.id.tvCourseInfo);

        lvCourses.setDivider(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        lvCourses.setDividerHeight(50);

        dbAvailability = FirebaseDatabase.getInstance().getReference("availability");

        courses = new ArrayList<>();
        // Use the new Adapter
        adapter = new CoursePillAdapter(this, courses);
        lvCourses.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

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