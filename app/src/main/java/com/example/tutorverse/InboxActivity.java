package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class InboxActivity extends AppCompatActivity {

    ListView lvInbox;

    ArrayList<InboxAdapter.InboxItem> inboxList;
    InboxAdapter adapter;
    String myUid;

    LinearLayout btnDashboard, btnInbox, btnEditProfile;
    TextView tvUnreadBadge;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);




        lvInbox = findViewById(R.id.lvInbox);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        inboxList = new ArrayList<>();
        adapter = new InboxAdapter(this, inboxList);
        lvInbox.setAdapter(adapter);

        lvInbox.setOnItemClickListener((parent, view, position, id) -> {
            InboxAdapter.InboxItem item = inboxList.get(position);
            Intent i = new Intent(InboxActivity.this, ChatActivity.class);
            i.putExtra("otherUid", item.otherUid);
            i.putExtra("otherName", item.otherName);
            startActivity(i);
        });

        loadInbox();

        setupNavbar();
        setupUnreadBadge();
    }

    private void setupNavbar() {
        btnDashboard = findViewById(R.id.btnDashBoard);
        btnInbox = findViewById(R.id.btnInbox);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        tvUnreadBadge = findViewById(R.id.tvUnreadBadge);

        btnDashboard.setOnClickListener(v -> startActivity(new Intent(this, StudentDashboardActivity.class)));

        btnInbox.setOnClickListener(v -> Toast.makeText(this, "Already in Messages", Toast.LENGTH_SHORT).show());

        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void setupUnreadBadge() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbChats = FirebaseDatabase.getInstance().getReference("user-chats").child(uid);

        dbChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unread = 0;
                 for (DataSnapshot chat : snapshot.getChildren()) {
                     Integer count = chat.child("unreadCount").getValue(Integer.class);
                     if (count != null)
                         unread += count;
                 }

                 if (unread > 0) {
                     tvUnreadBadge.setText(String.valueOf(unread));
                     tvUnreadBadge.setVisibility(TextView.VISIBLE);
                 } else {
                     tvUnreadBadge.setVisibility(TextView.GONE);
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void loadInbox() {
        DatabaseReference dbUserChats = FirebaseDatabase.getInstance().getReference("user-chats").child(myUid);

        dbUserChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                inboxList.clear();
                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String otherName = chatSnap.child("otherName").getValue(String.class);
                    String otherUid = chatSnap.child("otherUid").getValue(String.class);
                    String lastMsg = chatSnap.child("lastMessage").getValue(String.class);
                    Long ts = chatSnap.child("timestamp").getValue(Long.class);

                    if (otherName != null && ts != null) {
                        inboxList.add(new InboxAdapter.InboxItem(
                                chatSnap.getKey(), otherUid, otherName, lastMsg, ts
                        ));
                    }
                }

                // Sort by newest first
                Collections.sort(inboxList, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}