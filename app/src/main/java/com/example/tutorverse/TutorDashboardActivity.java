package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class TutorDashboardActivity extends AppCompatActivity {

    Button btnSetAvailability, btnViewBookings, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_dashboard);

        btnSetAvailability = findViewById(R.id.btnSetAvailability);
        btnViewBookings = findViewById(R.id.btnViewBookings);
        btnBack = findViewById(R.id.btnBack);

        btnSetAvailability.setOnClickListener(v ->
                startActivity(new Intent(this, TeacherAvailabilityActivity.class)));

        btnViewBookings.setOnClickListener(v ->
                startActivity(new Intent(this, TutorCalendarActivity.class)));

        btnBack.setOnClickListener(v -> finish());
    }
}
