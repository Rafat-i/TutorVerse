package com.example.tutorverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import models.ScheduleMeeting;

public class ScheduleMeetingAdapter extends ArrayAdapter<ScheduleMeeting> {

    public ScheduleMeetingAdapter(@NonNull Context context, @NonNull ArrayList<ScheduleMeeting> items) {
        super(context, 0, items);
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
            tvTime.setText(meeting.getTime());

            if (meeting.isBooked()) {
                btnBook.setText("Booked");
            } else {
                btnBook.setText("Book");
            }

            btnBook.setOnClickListener(v -> {
                if (!meeting.isBooked()) {
                    meeting.setBooked(true);
                    btnBook.setText("Booked");

                    Toast.makeText(
                            getContext(),
                            "Booked " + meeting.getTutorName() + " at " + meeting.getTime(),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            getContext(),
                            "This slot is already booked.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        return convertView;
    }
}
