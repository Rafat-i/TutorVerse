package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StudentDashboardActivity extends AppCompatActivity {

    Button btnEditProfile, btnBookMeeting, btnViewBookings, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);

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

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnBookMeeting = findViewById(R.id.btnBookMeeting);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnBack = findViewById(R.id.btnBack);

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnBookMeeting.setOnClickListener(v ->
                startActivity(new Intent(this, CourseSelectionActivity.class)));

        btnViewBookings.setOnClickListener(v ->
                startActivity(new Intent(this, StudentBookingsActivity.class)));

        btnBack.setOnClickListener(v -> finish());
    }
}