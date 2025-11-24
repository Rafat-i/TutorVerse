package com.example.tutorverse;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class InboxAdapter extends ArrayAdapter<InboxAdapter.InboxItem> {

    public static class InboxItem {
        public String chatRoomId;
        public String otherUid;
        public String otherName;
        public String lastMessage;
        public long timestamp;

        public InboxItem(String chatRoomId, String otherUid, String otherName, String lastMessage, long timestamp) {
            this.chatRoomId = chatRoomId;
            this.otherUid = otherUid;
            this.otherName = otherName;
            this.lastMessage = lastMessage;
            this.timestamp = timestamp;
        }
    }

    public InboxAdapter(@NonNull Context context, ArrayList<InboxItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_inbox_item, parent, false);
        }

        InboxItem item = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvLastMsg = convertView.findViewById(R.id.tvLastMsg);
        TextView tvTime = convertView.findViewById(R.id.tvTime);

        if (item != null) {
            tvName.setText(item.otherName);
            tvLastMsg.setText(item.lastMessage);
            tvTime.setText(getDate(item.timestamp));
        }

        return convertView;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time);
        return DateFormat.format("MMM dd, hh:mm a", cal).toString();
    }
}