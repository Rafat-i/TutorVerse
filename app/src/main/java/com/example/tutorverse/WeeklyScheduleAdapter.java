package com.example.tutorverse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.Map;

public class WeeklyScheduleAdapter extends BaseAdapter {

    public static class SlotInfo {
        public String course;
        public String studentName;
        public String studentUid;
        public String bookingKey;

        public SlotInfo(String course, String studentName, String studentUid, String bookingKey) {
            this.course = course;
            this.studentName = studentName;
            this.studentUid = studentUid;
            this.bookingKey = bookingKey;
        }
    }

    Context context;
    List<String> timeStarts;
    List<String> timeLabels;
    Map<String, SlotInfo> slotMap;

    public WeeklyScheduleAdapter(Context context,
                                 List<String> timeStarts,
                                 List<String> timeLabels,
                                 Map<String, SlotInfo> slotMap) {
        this.context = context;
        this.timeStarts = timeStarts;
        this.timeLabels = timeLabels;
        this.slotMap = slotMap;
    }

    @Override
    public int getCount() { return timeStarts.size(); }

    @Override
    public Object getItem(int position) { return timeStarts.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    private int colorFor(String course, boolean isBooked) {
        if (course == null) return 0xFFEEEEEE;

        String lower = course.toLowerCase();
        if (lower.contains("android")) return 0xFF81C784;
        if (lower.contains("java")) return 0xFF64B5F6;
        if (lower.contains("web")) return 0xFFFFB74D;
        if (lower.contains("data")) return 0xFFBA68C8;
        if (lower.contains("oop")) return 0xFF4DB6AC;
        return 0xFFA1887F;
    }

    private String getCourseCode(String course) {
        if (course == null) return "";
        String lower = course.toLowerCase();
        if (lower.contains("android")) return "AND";
        if (lower.contains("java")) return "JAVA";
        if (lower.contains("web")) return "WEB";
        if (lower.contains("data")) return "DSA";
        if (lower.contains("oop")) return "OOP";
        if (course.length() >= 3) return course.substring(0, 3).toUpperCase();
        return course.toUpperCase();
    }

    private void setupCell(TextView cell, String day, String startTime, String label) {
        String key = day + "|" + startTime;
        SlotInfo info = slotMap.get(key);

        if (info != null) {
            boolean isBooked = (info.studentUid != null);
            cell.setBackgroundColor(colorFor(info.course, isBooked));

            // If booked, show Student Name. If not, show Course Code.
            if (isBooked) {
                cell.setText("Booked\n" + info.studentName);
                cell.setTextSize(9); // Smaller text for name
                cell.setTextColor(Color.WHITE);
            } else {
                cell.setText(getCourseCode(info.course));
                cell.setTextSize(11);
                cell.setTextColor(Color.WHITE);
            }

            cell.setOnClickListener(v -> showDetailsDialog(day, label, info));
        } else {
            cell.setBackgroundColor(0xFFEEEEEE);
            cell.setText("");
            cell.setOnClickListener(null);
        }
    }

    private void showDetailsDialog(String day, String timeLabel, SlotInfo info) {
        String cleanTime = timeLabel.replace("\n", " - ");
        String status = (info.studentUid != null) ? "Booked by " + info.studentName : "Available (Empty)";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(info.course);
        builder.setMessage("Day: " + day + "\nTime: " + cleanTime + "\nStatus: " + status);

        // If booked, add Chat button
        if (info.studentUid != null) {
            builder.setPositiveButton("Message " + info.studentName, (dialog, which) -> {
                Intent i = new Intent(context, ChatActivity.class);
                i.putExtra("otherUid", info.studentUid);
                i.putExtra("otherName", info.studentName);
                context.startActivity(i);
            });
        }

        builder.setNegativeButton("Close", null);
        builder.show();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String startTime = timeStarts.get(position);
        String label = timeLabels.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_week_slot, parent, false);
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