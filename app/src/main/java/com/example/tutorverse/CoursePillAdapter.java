package com.example.tutorverse;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class CoursePillAdapter extends ArrayAdapter<String> {

    public CoursePillAdapter(@NonNull Context context, ArrayList<String> courses) {
        super(context, 0, courses);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_course_pill, parent, false);
        }

        String course = getItem(position);
        TextView tvCourseName = convertView.findViewById(R.id.tvCourseName);
        CardView cardContainer = convertView.findViewById(R.id.cardContainer);

        tvCourseName.setText(course);

        // Color Coordination Logic
        int color = 0xFFA1887F; // Default Brown
        if (course != null) {
            String lower = course.toLowerCase();
            if (lower.contains("android")) color = 0xFF81C784;      // Green
            else if (lower.contains("java")) color = 0xFF64B5F6;    // Blue
            else if (lower.contains("web")) color = 0xFFFFB74D;     // Orange
            else if (lower.contains("data")) color = 0xFFBA68C8;    // Purple
            else if (lower.contains("oop")) color = 0xFF4DB6AC;     // Teal
        }

        cardContainer.setCardBackgroundColor(color);

        return convertView;
    }
}