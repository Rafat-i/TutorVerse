package com.example.tutorverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.Map;

public class WeeklyScheduleAdapter extends BaseAdapter {

    Context context;
    List<String> timeStarts;
    List<String> timeLabels;
    Map<String, String> slotCourseMap;

    public WeeklyScheduleAdapter(Context context,
                                 List<String> timeStarts,
                                 List<String> timeLabels,
                                 Map<String, String> slotCourseMap) {
        this.context = context;
        this.timeStarts = timeStarts;
        this.timeLabels = timeLabels;
        this.slotCourseMap = slotCourseMap;
    }

    @Override
    public int getCount() {
        return timeStarts.size();
    }

    @Override
    public Object getItem(int position) {
        return timeStarts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int colorFor(String course) {
        if (course == null) return 0xFFEEEEEE; // Lighter grey for empty

        String lower = course.toLowerCase();
        if (lower.contains("android")) return 0xFF81C784;
        else if (lower.contains("java")) return 0xFF64B5F6;
        else if (lower.contains("web")) return 0xFFFFB74D;
        else if (lower.contains("data")) return 0xFFBA68C8;
        else if (lower.contains("oop")) return 0xFF4DB6AC;
        else return 0xFFA1887F;
    }

    // Helper to generate 3-4 letter codes
    private String getCourseCode(String course) {
        if (course == null) return "";
        String lower = course.toLowerCase();
        if (lower.contains("android")) return "AND";
        if (lower.contains("java")) return "JAVA";
        if (lower.contains("web")) return "WEB";
        if (lower.contains("data")) return "DSA";
        if (lower.contains("oop")) return "OOP";
        // Fallback: first 3 letters uppercase
        if (course.length() >= 3) return course.substring(0, 3).toUpperCase();
        return course.toUpperCase();
    }

    private void setupCell(TextView cell, String day, String startTime, String label) {
        String key = day + "|" + startTime;
        String course = slotCourseMap.get(key);

        cell.setBackgroundColor(colorFor(course));

        // Set the abbreviation code inside the cell
        if (course != null) {
            cell.setText(getCourseCode(course));
        } else {
            cell.setText("");
        }

        cell.setOnClickListener(v -> {
            if (course != null) {
                showClassDialog(day, label, course);
            }
        });
    }

    private void showClassDialog(String day, String timeLabel, String courseName) {
        // Replace the newline with a cleaner dash format
        String cleanTime = timeLabel.replace("\n", " - ");

        new AlertDialog.Builder(context)
                .setTitle(courseName)
                .setMessage("Day: " + day + "\nTime: " + cleanTime + "\nStatus: Scheduled")
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String startTime = timeStarts.get(position);
        String label = timeLabels.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.row_week_slot, parent, false);
        }

        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView cellMon = convertView.findViewById(R.id.cellMon);
        TextView cellTue = convertView.findViewById(R.id.cellTue);
        TextView cellWed = convertView.findViewById(R.id.cellWed);
        TextView cellThu = convertView.findViewById(R.id.cellThu);
        TextView cellFri = convertView.findViewById(R.id.cellFri);

        tvTime.setText(label);

        setupCell(cellMon, "Monday", startTime, label);
        setupCell(cellTue, "Tuesday", startTime, label);
        setupCell(cellWed, "Wednesday", startTime, label);
        setupCell(cellThu, "Thursday", startTime, label);
        setupCell(cellFri, "Friday", startTime, label);

        return convertView;
    }
}