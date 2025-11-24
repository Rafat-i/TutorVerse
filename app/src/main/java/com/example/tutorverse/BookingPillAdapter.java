package com.example.tutorverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class BookingPillAdapter extends ArrayAdapter<BookingPillAdapter.BookingItem> {

    // Simple inner class to hold data
    public static class BookingItem {
        public String tutorName;
        public String tutorUid;
        public String bookingKey;
        public String course;
        public String time;

        public BookingItem(String tutorName, String tutorUid, String bookingKey, String course, String time) {
            this.tutorName = tutorName;
            this.tutorUid = tutorUid;
            this.bookingKey = bookingKey;
            this.course = course;
            this.time = time;
        }
    }

    public BookingPillAdapter(@NonNull Context context, ArrayList<BookingItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_booking_pill, parent, false);
        }

        BookingItem item = getItem(position);

        CardView cardBooking = convertView.findViewById(R.id.cardBooking);
        TextView tvCourse = convertView.findViewById(R.id.tvCourse);
        TextView tvTutor = convertView.findViewById(R.id.tvTutor);
        TextView tvTime = convertView.findViewById(R.id.tvTime);

        if (item != null) {
            tvCourse.setText(item.course);
            tvTutor.setText("Tutor: " + item.tutorName);
            tvTime.setText(item.time);

            // Color Logic
            int color = 0xFFA1887F;
            String lower = item.course.toLowerCase();
            if (lower.contains("android")) color = 0xFF81C784;
            else if (lower.contains("java")) color = 0xFF64B5F6;
            else if (lower.contains("web")) color = 0xFFFFB74D;
            else if (lower.contains("data")) color = 0xFFBA68C8;
            else if (lower.contains("oop")) color = 0xFF4DB6AC;

            cardBooking.setCardBackgroundColor(color);
        }

        return convertView;
    }
}