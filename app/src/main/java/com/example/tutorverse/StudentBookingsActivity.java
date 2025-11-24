package com.example.tutorverse;

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

public class StudentBookingsActivity extends AppCompatActivity {

    ListView lvMyBookings;
    Button btnBack;

    // Changed to use the custom BookingItem class
    ArrayList<BookingPillAdapter.BookingItem> myBookingsList;
    BookingPillAdapter adapter;

    String myUid;

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
            showCancelDialog(position);
        });

        loadMyBookings();
    }

    private void loadMyBookings() {
        DatabaseReference dbBookings = FirebaseDatabase.getInstance().getReference("bookings");
        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("users");

        dbBookings.get().addOnSuccessListener(snapshot -> {
            myBookingsList.clear();

            for (DataSnapshot tutorSnap : snapshot.getChildren()) {
                String tutorUid = tutorSnap.getKey();

                for (DataSnapshot bookingSnap : tutorSnap.getChildren()) {
                    String studentUid = bookingSnap.child("studentUid").getValue(String.class);

                    if (studentUid != null && studentUid.equals(myUid)) {
                        String course = bookingSnap.child("course").getValue(String.class);
                        String time = bookingSnap.child("time").getValue(String.class);
                        String bookingKey = bookingSnap.getKey();

                        // We need the tutor's name. Fetch it!
                        if (tutorUid != null) {
                            dbUsers.child(tutorUid).child("username").get().addOnSuccessListener(nameSnap -> {
                                String tutorName = nameSnap.getValue(String.class);
                                if (tutorName == null) tutorName = "Unknown Tutor";

                                // Add to list and refresh
                                myBookingsList.add(new BookingPillAdapter.BookingItem(
                                        tutorName, tutorUid, bookingKey, course, time
                                ));
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                }
            }
        });
    }

    private void showCancelDialog(int position) {
        if (myBookingsList.isEmpty() || position >= myBookingsList.size()) return;

        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking?")
                .setMessage("Are you sure you want to cancel this session?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    BookingPillAdapter.BookingItem item = myBookingsList.get(position);

                    FirebaseDatabase.getInstance().getReference("bookings")
                            .child(item.tutorUid).child(item.bookingKey).removeValue()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(this, "Booking Cancelled", Toast.LENGTH_SHORT).show();
                                // Remove from local list to update UI instantly
                                myBookingsList.remove(position);
                                adapter.notifyDataSetChanged();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }
}