package com.example.tutorverse;

import android.content.Context;
import android.content.Intent;
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
    String currentStudentName = "Student";

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
        TextView tvRating = convertView.findViewById(R.id.tvRating);
        TextView tvSubject = convertView.findViewById(R.id.tvSubject);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        Button btnBook = convertView.findViewById(R.id.btnBook);

        if (meeting != null) {
            tvTutorName.setText(meeting.getTutorName());
            tvTutorName.setTextColor(0xFF1A3D7C);
            tvTutorName.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), TutorProfileActivity.class);
                i.putExtra("tutorUid", meeting.getTutorUid());
                i.putExtra("tutorName", meeting.getTutorName());
                getContext().startActivity(i);
            });

            if (meeting.rating > 0) {
                tvRating.setText(String.format("★ %.1f", meeting.rating));
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }

            tvSubject.setText(meeting.getSubject());
            tvTime.setText(meeting.getTime());

            if (meeting.isBooked()) {
                if (currentStudentName.equals(meeting.getTakenBy())) {
                    btnBook.setText("Booked");
                    btnBook.setBackgroundColor(0xFF4CAF50);
                    btnBook.setEnabled(false);
                } else {
                    btnBook.setText("Unavailable");
                    btnBook.setBackgroundColor(0xFFE0E0E0);
                    btnBook.setTextColor(0xFF888888);
                    btnBook.setEnabled(false);
                }
            } else {
                btnBook.setText("Book");
                btnBook.setBackgroundColor(0xFF1A3D7C);
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

        for (int i = 0; i < meeting.durationHours; i++) {
            int startHour = Integer.parseInt(meeting.getStartTime().split(":")[0]);
            int currentSlot = startHour + i;
            String timeString = String.format("%02d:00", currentSlot);

            String key = meeting.getDay() + "|" + timeString;

            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("studentName", currentStudentName);
            bookingData.put("studentUid", currentStudentUid);
            bookingData.put("course", meeting.getSubject());
            bookingData.put("time", meeting.getDay() + " " + timeString);

            dbBookings.child(meeting.getTutorUid()).child(key).setValue(bookingData);
        }

        Toast.makeText(getContext(), "Block Booking Confirmed!", Toast.LENGTH_SHORT).show();
        meeting.setBooked(true);
        meeting.setTakenBy(currentStudentName);
        notifyDataSetChanged();
    }
}