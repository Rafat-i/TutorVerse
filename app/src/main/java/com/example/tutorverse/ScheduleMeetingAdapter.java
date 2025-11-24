package com.example.tutorverse;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import models.ScheduleMeeting;

public class ScheduleMeetingAdapter extends ArrayAdapter<ScheduleMeeting> {

    String currentStudentUid;
    String currentStudentName = "Student"; // Placeholder until we fetch profile

    public ScheduleMeetingAdapter(@NonNull Context context, @NonNull ArrayList<ScheduleMeeting> items) {
        super(context, 0, items);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentStudentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            fetchStudentName();
        }
    }

    private void fetchStudentName() {
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentStudentUid).child("username").get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) currentStudentName = snap.getValue(String.class);
                });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.row_schedule_meeting, parent, false);
        }

        ScheduleMeeting meeting = getItem(position);

        TextView tvTutorName = convertView.findViewById(R.id.tvTutorName);
        TextView tvSubject = convertView.findViewById(R.id.tvSubject);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        Button btnBook = convertView.findViewById(R.id.btnBook);

        if (meeting != null) {
            tvTutorName.setText(meeting.getTutorName());
            tvSubject.setText(meeting.getSubject());
            tvTime.setText(meeting.getTime()); // e.g., "Monday 08:00"

            // Logic for Button State
            if (meeting.isBooked()) {
                // Check if *I* booked it
                if (currentStudentName.equals(meeting.getTakenBy())) {
                    btnBook.setText("Booked");
                    btnBook.setBackgroundColor(0xFF4CAF50); // Green
                    btnBook.setEnabled(false);
                } else {
                    // Someone else booked it
                    btnBook.setText("Unavailable");
                    btnBook.setBackgroundColor(0xFFE0E0E0); // Grey
                    btnBook.setTextColor(0xFF888888);
                    btnBook.setEnabled(false);
                }
            } else {
                // Available
                btnBook.setText("Book");
                btnBook.setBackgroundColor(0xFF1A3D7C); // Blue
                btnBook.setTextColor(0xFFFFFFFF);
                btnBook.setEnabled(true);
            }

            btnBook.setOnClickListener(v -> {
                if (!meeting.isBooked()) {
                    bookSession(meeting);
                }
            });
        }

        return convertView;
    }

    private void bookSession(ScheduleMeeting meeting) {
        DatabaseReference dbBookings = FirebaseDatabase.getInstance().getReference("bookings");

        String key = meeting.getDay() + "|" + meeting.getStartTime();

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("studentName", currentStudentName);
        bookingData.put("studentUid", currentStudentUid);
        bookingData.put("course", meeting.getSubject());
        bookingData.put("time", meeting.getTime());

        dbBookings.child(meeting.getTutorUid()).child(key).setValue(bookingData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Booking Confirmed!", Toast.LENGTH_SHORT).show();
                    meeting.setBooked(true);
                    meeting.setTakenBy(currentStudentName);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to book.", Toast.LENGTH_SHORT).show()
                );
    }
}