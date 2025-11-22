package com.example.tutorverse;

import android.os.Bundle;
import android.widget.CalendarView;
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
import java.util.Calendar;
import java.util.Locale;

public class TutorCalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    ListView lvBookings;

    FirebaseAuth auth;
    DatabaseReference dbAvailabilityCourses;

    ArrayList<String> items;
    BookingListAdapter adapter;   // simple ArrayAdapter<String> we made earlier

    String tutorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_calendar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        calendarView = findViewById(R.id.calendarView);
        lvBookings = findViewById(R.id.lvBookings);

        auth = FirebaseAuth.getInstance();
        tutorUid = auth.getCurrentUser().getUid();

        dbAvailabilityCourses = FirebaseDatabase.getInstance()
                .getReference("availability")
                .child(tutorUid)
                .child("courses");

        items = new ArrayList<>();
        adapter = new BookingListAdapter(this, items);
        lvBookings.setAdapter(adapter);

        long todayMillis = calendarView.getDate();
        String todayDayName = getDayNameFromMillis(todayMillis);
        loadAvailabilityForDay(todayDayName);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            String dayName = getDayNameFromMillis(c.getTimeInMillis());
            loadAvailabilityForDay(dayName);
        });
    }

    private String getDayNameFromMillis(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            case Calendar.SUNDAY: return "Sunday";
        }
        return "Monday";
    }

    private void loadAvailabilityForDay(String dayName) {
        dbAvailabilityCourses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                items.clear();

                if (!snapshot.exists()) {
                    items.add("No availability saved.");
                    adapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String courseName = courseSnap.getKey();
                    DataSnapshot daySnap = courseSnap.child(dayName);

                    if (daySnap.exists()) {
                        String start = daySnap.child("start").getValue(String.class);
                        String end = daySnap.child("end").getValue(String.class);

                        String line = courseName + " - " + dayName + " " + start + " to " + end;
                        items.add(line);
                    }
                }

                if (items.isEmpty()) {
                    items.add("No availability on " + dayName);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TutorCalendarActivity.this,
                        "Error loading availability", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
