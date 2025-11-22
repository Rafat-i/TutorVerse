package com.example.tutorverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ScheduleGridAdapter extends BaseAdapter {

    private final Context context;
    private final String[] dayShort = {"Mon", "Tue", "Wed", "Thu", "Fri"};
    private final String[] dayFull = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

    public ScheduleGridAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return dayShort.length;
    }

    @Override
    public Object getItem(int position) {
        return dayFull[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getFullDay(int position) {
        return dayFull[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.row_schedule_day, parent, false);
        }

        TextView tvDay = convertView.findViewById(R.id.tvDay);
        tvDay.setText(dayShort[position]);

        return convertView;
    }
}
