package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudentBookingsActivity extends AppCompatActivity {

    ListView lvMyBookings;
    Button btnBack;

    ArrayList<BookingPillAdapter.BookingItem> myBookingsList;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_bookings);

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

        lvMyBookings = findViewById(R.id.lvMyBookings);
        btnBack = findViewById(R.id.btnBack);

        lvMyBookings.setDivider(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        lvMyBookings.setDividerHeight(50);

        btnBack.setOnClickListener(v -> finish());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myBookingsList = new ArrayList<>();

        adapter = new BookingPillAdapter(this, myBookingsList);
        lvMyBookings.setAdapter(adapter);

        lvMyBookings.setOnItemClickListener((parent, view, position, id) -> {
            showActionDialog(position);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyBookings();
    }

    private void loadMyBookings() {
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
                        String fullTime = bookingSnap.child("time").getValue(String.class); // "Monday 08:00"
                        String bookingKey = bookingSnap.getKey();

                        if (fullTime != null) {
                            String[] parts = fullTime.split(" ");
                            if (parts.length >= 2) {
                                String day = parts[0];
                                int hour = Integer.parseInt(parts[1].split(":")[0]);

                                rawList.add(new RawBooking(tutorUid, "Unknown", course, day, hour, bookingKey));
                            }
                        }
                    }
                }
            }

            // 2. Fetch Tutor Names & Process
            fetchNamesAndGroup(rawList, dbUsers);
        });
    }

    private void fetchNamesAndGroup(List<RawBooking> rawList, DatabaseReference dbUsers) {
        dbUsers.get().addOnSuccessListener(userSnap -> {
            for (RawBooking b : rawList) {
                String name = userSnap.child(b.tutorUid).child("username").getValue(String.class);
                if (name != null) b.tutorName = name;
            }

            myBookingsList.clear();

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

                if (next.tutorUid.equals(currentBlock.tutorUid) &&
                        next.course.equals(currentBlock.course) &&
                        next.day.equals(currentBlock.day) &&
                        next.startHour == endHour) {

                    endHour++;
                    combinedKeys += "," + next.bookingKey;
                } else {
                    addMergedItem(currentBlock, endHour, combinedKeys);

                    currentBlock = next;
                    endHour = next.startHour + 1;
                    combinedKeys = next.bookingKey;
                }
            }
            // Commit final block
            addMergedItem(currentBlock, endHour, combinedKeys);

            adapter.notifyDataSetChanged();
        });
    }

    private void addMergedItem(RawBooking start, int endHour, String allKeys) {
        String timeStr = String.format("%s %02d:00 - %02d:00", start.day, start.startHour, endHour);

        myBookingsList.add(new BookingPillAdapter.BookingItem(
                start.tutorName,
                start.tutorUid,
                allKeys,
                start.course,
                timeStr
        ));
    }

    private void showActionDialog(int position) {
        if (myBookingsList.isEmpty() || position >= myBookingsList.size()) return;

        BookingPillAdapter.BookingItem item = myBookingsList.get(position);

        String[] options = {"Chat with Tutor", "Complete Session & Review", "Cancel Booking"};

        new AlertDialog.Builder(this)
                .setTitle("Manage Booking")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent i = new Intent(StudentBookingsActivity.this, ChatActivity.class);
                        i.putExtra("otherUid", item.tutorUid);
                        i.putExtra("otherName", item.tutorName);
                        startActivity(i);
                    } else if (which == 1) {
                        Intent i = new Intent(StudentBookingsActivity.this, ReviewActivity.class);
                        i.putExtra("tutorUid", item.tutorUid);
                        i.putExtra("tutorName", item.tutorName);

                        String firstKey = item.bookingKey.split(",")[0];
                        i.putExtra("bookingKey", firstKey);

                        deleteMultiBooking(item.tutorUid, item.bookingKey);

                        startActivity(i);
                    } else {
                        deleteMultiBooking(item.tutorUid, item.bookingKey);
                        Toast.makeText(this, "Booking Cancelled", Toast.LENGTH_SHORT).show();
                        myBookingsList.remove(position);
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
}