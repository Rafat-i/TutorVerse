package com.example.tutorverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

    private int colorFor(String day, String timeStart) {
        String key = day + "|" + timeStart;
        String course = slotCourseMap.get(key);
        if (course == null) {
            return 0xFFE0E0E0;
        }

        String lower = course.toLowerCase();

        if (lower.contains("android")) {
            return 0xFF81C784;
        } else if (lower.contains("java")) {
            return 0xFF64B5F6;
        } else if (lower.contains("web")) {
            return 0xFFFFB74D;
        } else if (lower.contains("data")) {
            return 0xFFBA68C8;
        } else if (lower.contains("oop")) {
            return 0xFF4DB6AC;
        } else {
            return 0xFFA1887F;
        }
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

        cellMon.setBackgroundColor(colorFor("Monday", startTime));
        cellTue.setBackgroundColor(colorFor("Tuesday", startTime));
        cellWed.setBackgroundColor(colorFor("Wednesday", startTime));
        cellThu.setBackgroundColor(colorFor("Thursday", startTime));
        cellFri.setBackgroundColor(colorFor("Friday", startTime));

        cellMon.setText("");
        cellTue.setText("");
        cellWed.setText("");
        cellThu.setText("");
        cellFri.setText("");

        return convertView;
    }
}
