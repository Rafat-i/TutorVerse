package com.example.tutorverse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BookingListAdapter extends ArrayAdapter<String> {

    public BookingListAdapter(Context context, ArrayList<String> data) {
        super(context, android.R.layout.simple_list_item_1, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTextColor(0xFF1A3D7C);
        view.setTextSize(16);
        return view;
    }
}
