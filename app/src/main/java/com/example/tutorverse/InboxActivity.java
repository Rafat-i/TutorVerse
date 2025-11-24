package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.util.ArrayList;
import java.util.Collections;

public class InboxActivity extends AppCompatActivity {

    ListView lvInbox;
    Button btnBack;
    ArrayList<InboxAdapter.InboxItem> inboxList;
    InboxAdapter adapter;
    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inbox);

        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + paddingPx,
                    systemBars.top + paddingPx,
                    systemBars.right + paddingPx,
                    systemBars.bottom + paddingPx
            );
            return insets;
        });

        lvInbox = findViewById(R.id.lvInbox);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

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