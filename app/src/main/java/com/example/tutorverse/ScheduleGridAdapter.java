package com.example.tutorverse;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ScheduleGridAdapter extends BaseAdapter {

    Context context;
    String[] daysShort = {"Mon", "Tue", "Wed", "Thu", "Fri"};
    String[] daysFull = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    int selectedPosition = -1;

    public ScheduleGridAdapter(Context context) {
        this.context = context;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public String getFullDay(int position) {
        return daysFull[position];
    }

    @Override
    public int getCount() { return daysShort.length; }

    @Override
    public Object getItem(int position) { return daysShort[position]; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
            tv = new TextView(context);
            tv.setLayoutParams(new ViewGroup.LayoutParams(150, 100)); // Fixed size square
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
        } else {
            tv = (TextView) convertView;
        }

        tv.setText(daysShort[position]);

        if (position == selectedPosition) {
            tv.setBackgroundColor(0xFF1A3D7C); // Dark Blue Highlight
            tv.setTextColor(Color.WHITE);
        } else {
            tv.setBackgroundColor(0xFFEEEEEE); // Light Grey
            tv.setTextColor(Color.BLACK);
        }

        return tv;
    }
}