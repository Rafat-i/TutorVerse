package com.example.tutorverse;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {

    public static class ChatMessage {
        public String senderUid;
        public String text;
        public ChatMessage(String senderUid, String text) {
            this.senderUid = senderUid;
            this.text = text;
        }
    }

    Context context;
    ArrayList<ChatMessage> messages;
    String myUid;

    public ChatAdapter(Context context, ArrayList<ChatMessage> messages, String myUid) {
        this.context = context;
        this.messages = messages;
        this.myUid = myUid;
    }

    @Override
    public int getCount() { return messages.size(); }

    @Override
    public Object getItem(int position) { return messages.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_chat_message, parent, false);
        }

        ChatMessage msg = messages.get(position);

        LinearLayout layoutBubble = convertView.findViewById(R.id.layoutBubble);
        TextView tvMessage = convertView.findViewById(R.id.tvMessage);
        LinearLayout rootLayout = convertView.findViewById(R.id.rootLayout);

        tvMessage.setText(msg.text);

        if (msg.senderUid.equals(myUid)) {
            // My Message (Right)
            rootLayout.setGravity(Gravity.END);
            layoutBubble.setBackgroundResource(R.drawable.bg_chat_me);
            tvMessage.setTextColor(0xFFFFFFFF); // White
        } else {
            // Their Message (Left)
            rootLayout.setGravity(Gravity.START);
            layoutBubble.setBackgroundResource(R.drawable.bg_chat_other);
            tvMessage.setTextColor(0xFF333333); // Dark Grey
        }

        return convertView;
    }
}