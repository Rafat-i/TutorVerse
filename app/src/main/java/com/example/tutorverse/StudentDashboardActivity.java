package com.example.tutorverse;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity {

    LinearLayout btnDashboard, btnInbox, btnEditProfile;

    TextView tvUnreadBadge; // New
    ListView lvMyBookings;

    FloatingActionButton btnAdd;

    ArrayList<BookingPillAdapter.BookingItem> myBookingList;
    BookingPillAdapter adapter;
    String myUid;

    private static class RawBooking {
        String tutorUid;
        String tutorName;
        String course;
        String day;
        int startHour;
        String bookingKey;

        public RawBooking(String tutorUid, String tutorName, String course, String day, int startHour, String bookingKey) {
            this.tutorUid = tutorUid;
            this.tutorName = tutorName;
            this.course = course;
            this.day = day;
            this.startHour = startHour;
            this.bookingKey = bookingKey;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_student_dashboard);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnDashboard = findViewById(R.id.btnDashBoard);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        btnInbox = findViewById(R.id.btnInbox);
        tvUnreadBadge = findViewById(R.id.tvUnreadBadge); // Init Badge

        lvMyBookings = findViewById(R.id.lvMyBookings);

        btnAdd = findViewById(R.id.btnAddBooking);

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, CourseSelectionActivity.class));
        });

        myBookingList = new ArrayList<>();
        adapter = new BookingPillAdapter(this, myBookingList);
        lvMyBookings.setAdapter(adapter);

        lvMyBookings.setDivider(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        lvMyBookings.setDividerHeight(50);

        lvMyBookings.setOnItemClickListener((parent, view, position, id) -> {
            showActionDialog(position);
        });

        btnDashboard.setOnClickListener(v -> loadBookings());

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));


        btnInbox.setOnClickListener(v ->
                startActivity(new Intent(this, InboxActivity.class)));




        setupBadgeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }


    private void loadBookings() {
        DatabaseReference dbBookings = FirebaseDatabase.getInstance().getReference("bookings");
        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("users");

        dbBookings.get().addOnSuccessListener(snapshot -> {
            List<RawBooking> rawList = new ArrayList<>();

            for (DataSnapshot tutorSnap : snapshot.getChildren()) {
                String tutorUid = tutorSnap.getKey();

                for (DataSnapshot bookingSnap : tutorSnap.getChildren()) {
                    String studentUid = bookingSnap.child("studentUid").getValue(String.class);

                    if (studentUid != null && studentUid.equals(myUid)) {
                        String course = bookingSnap.child("course").getValue(String.class);
                        String fullTime = bookingSnap.child("time").getValue(String.class);
                        String bookingKey = bookingSnap.getKey();

                        if (fullTime != null) {
                            String[] parts = fullTime.split(" ");
                            if (parts.length >= 2) {
                                String day = parts[0];
                                int hours = Integer.parseInt(parts[1].split(":")[0]);

                                rawList.add(new RawBooking(tutorUid, "Unknown", course, day, hours, bookingKey));
                            }
                        }
                    }
                }
            }
            fetchNamesAndGroup(rawList, dbUsers);
        });
    }

    private void fetchNamesAndGroup(List<RawBooking> rawList, DatabaseReference dbUsers) {
        dbUsers.get().addOnSuccessListener(userSnap -> {
            for (RawBooking b : rawList) {
                String name = userSnap.child(b.tutorUid).child("username").getValue(String.class);
                if (name != null) b.tutorName = name;
            }

            myBookingList.clear();

            Collections.sort(rawList, (o1, o2) -> {
                int dayComp = o1.day.compareTo(o2.day);
                if (dayComp != 0) return dayComp;
                return Integer.compare(o1.startHour, o2.startHour);
            });

            if (rawList.isEmpty()) {
                adapter.notifyDataSetChanged();
                return;
            }

            RawBooking currentBlock = rawList.get(0);
            int endHour = currentBlock.startHour + 1;
            String combinedKeys = currentBlock.bookingKey;

            for (int i = 1; i < rawList.size(); i++) {
                RawBooking next = rawList.get(i);

                if (next.tutorUid.equals(currentBlock.tutorUid) && next.course.equals(currentBlock.course) &&
                        next.day.equals(currentBlock.day) && next.startHour == endHour) {
                    endHour++;
                    combinedKeys += "," + next.bookingKey;
                } else {
                    addMergedItem(currentBlock, endHour, combinedKeys);

                    currentBlock = next;
                    endHour = next.startHour + 1;
                    combinedKeys = next.bookingKey;
                }
            }

            addMergedItem(currentBlock, endHour, combinedKeys);

            adapter.notifyDataSetChanged();
        });
    }

    private void addMergedItem(RawBooking start, int endHour, String allKeys) {
        String timeStr = String.format("%s %02d:00 - %02d:00", start.day, start.startHour, endHour);

        myBookingList.add(new BookingPillAdapter.BookingItem(start.tutorName, start.tutorUid, allKeys, start.course, timeStr));
    }

    private void showActionDialog(int position) {
        if (myBookingList.isEmpty() || position >= myBookingList.size()) return;

        BookingPillAdapter.BookingItem item = myBookingList.get(position);
        String[] options = {"Chat with Tutor", "Complete Session & Review", "Cancel Booking"};

        new AlertDialog.Builder(StudentDashboardActivity.this).setTitle("Manage Booking").setItems(options, (dialog, which) -> {
                    if(which == 0) {
                        Intent i = new Intent(StudentDashboardActivity.this, ChatActivity.class);
                        i.putExtra("otherUid", item.tutorUid);
                        i.putExtra("otherName", item.tutorName);
                        startActivity(i);
                    } else if (which == 1) {
                        Intent i = new Intent(StudentDashboardActivity.this, ReviewActivity.class);
                        i.putExtra("tutorUid", item.tutorUid);
                        i.putExtra("tutorName", item.tutorName);

                        String firstKey = item.bookingKey.split(",")[0];
                        i.putExtra("bookingKey", firstKey);

                        deleteMultiBooking(item.tutorUid, item.bookingKey);
                        startActivity(i);
                    } else {
                        deleteMultiBooking(item.tutorUid, item.bookingKey);
                        Toast.makeText(StudentDashboardActivity.this, "Booking Cancelled", Toast.LENGTH_LONG).show();
                        myBookingList.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    private void deleteMultiBooking(String tutorUid, String commaSeparatedKeys) {
        String[] keys = commaSeparatedKeys.split(",");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("bookings").child(tutorUid);
        for (String key : keys) {
            ref.child(key).removeValue();
        }
    }

    private void setupBadgeListener() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbChats = FirebaseDatabase.getInstance().getReference("user-chats").child(uid);

        dbChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalUnread = 0;
                for (DataSnapshot chat : snapshot.getChildren()) {
                    Integer count = chat.child("unreadCount").getValue(Integer.class);
                    if (count != null) {
                        totalUnread += count;
                    }
                }

                if (totalUnread > 0) {
                    tvUnreadBadge.setText(String.valueOf(totalUnread));
                    tvUnreadBadge.setVisibility(TextView.VISIBLE);
                } else {
                    tvUnreadBadge.setVisibility(TextView.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}