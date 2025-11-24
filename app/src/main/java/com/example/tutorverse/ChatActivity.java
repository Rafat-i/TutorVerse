package com.example.tutorverse;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ListView lvChat;
    EditText edMessage;
    ImageButton btnSend;
    TextView tvChatTitle;
    Button btnBack;

    String currentUid, otherUid, otherName;
    String chatRoomId;

    ArrayList<ChatAdapter.ChatMessage> messages;
    ChatAdapter adapter;
    DatabaseReference dbMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUid = getIntent().getStringExtra("otherUid");
        otherName = getIntent().getStringExtra("otherName");

        if (currentUid.compareTo(otherUid) < 0) {
            chatRoomId = currentUid + "_" + otherUid;
        } else {
            chatRoomId = otherUid + "_" + currentUid;
        }

        tvChatTitle = findViewById(R.id.tvChatTitle);
        lvChat = findViewById(R.id.lvChat);
        edMessage = findViewById(R.id.edMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        tvChatTitle.setText("Chat with " + otherName);

        btnBack.setOnClickListener(v -> finish());

        messages = new ArrayList<>();
        adapter = new ChatAdapter(this, messages, currentUid);
        lvChat.setAdapter(adapter);

        dbMessages = FirebaseDatabase.getInstance().getReference("messages").child(chatRoomId);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
        resetUnreadCount(); // Reset count when opening chat
    }

    private void resetUnreadCount() {
        FirebaseDatabase.getInstance().getReference("user-chats")
                .child(currentUid).child(otherUid).child("unreadCount").setValue(0);
    }

    private void sendMessage() {
        String txt = edMessage.getText().toString().trim();
        if (txt.isEmpty()) return;

        long timestamp = System.currentTimeMillis();

        // 1. Save Message
        Map<String, Object> msg = new HashMap<>();
        msg.put("senderUid", currentUid);
        msg.put("text", txt);
        msg.put("timestamp", timestamp);
        dbMessages.push().setValue(msg);

        DatabaseReference dbUserChats = FirebaseDatabase.getInstance().getReference("user-chats");

        FirebaseDatabase.getInstance().getReference("users").child(currentUid).child("username").get()
                .addOnSuccessListener(snap -> {
                    String myName = snap.getValue(String.class);
                    if (myName == null) myName = "User";

                    // 2. Update MY entry (unread = 0 for me)
                    Map<String, Object> myUpdates = new HashMap<>();
                    myUpdates.put("chatRoomId", chatRoomId);
                    myUpdates.put("otherUid", otherUid);
                    myUpdates.put("otherName", otherName);
                    myUpdates.put("lastMessage", "You: " + txt);
                    myUpdates.put("timestamp", timestamp);
                    // We DON'T reset unreadCount here, we let logic handle it,
                    // but usually for 'me' it stays 0.
                    dbUserChats.child(currentUid).child(otherUid).updateChildren(myUpdates);

                    // 3. Update OTHER PERSON'S entry (unread + 1)
                    DatabaseReference otherRef = dbUserChats.child(otherUid).child(currentUid);

                    // Update basic info first
                    Map<String, Object> otherUpdates = new HashMap<>();
                    otherUpdates.put("chatRoomId", chatRoomId);
                    otherUpdates.put("otherUid", currentUid);
                    otherUpdates.put("otherName", myName);
                    otherUpdates.put("lastMessage", txt);
                    otherUpdates.put("timestamp", timestamp);
                    otherRef.updateChildren(otherUpdates);

                    // Increment unreadCount transactionally
                    otherRef.child("unreadCount").runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer count = currentData.getValue(Integer.class);
                            if (count == null) {
                                currentData.setValue(1);
                            } else {
                                currentData.setValue(count + 1);
                            }
                            return Transaction.success(currentData);
                        }
                        @Override
                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {}
                    });
                });

        edMessage.setText("");
    }

    private void loadMessages() {
        dbMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String sender = snap.child("senderUid").getValue(String.class);
                    String text = snap.child("text").getValue(String.class);
                    messages.add(new ChatAdapter.ChatMessage(sender, text));
                }
                adapter.notifyDataSetChanged();
                lvChat.setSelection(adapter.getCount() - 1);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}