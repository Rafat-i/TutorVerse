package com.example.tutorverse;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TutorProfileActivity extends AppCompatActivity {

    TextView tvTutorName, tvAvgRating, tvBio;
    Button btnMessage, btnBack;
    ListView lvReviews;

    String tutorUid, tutorName;
    ArrayList<ReviewListAdapter.ReviewItem> reviewsList;
    ReviewListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutor_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tutorUid = getIntent().getStringExtra("tutorUid");
        tutorName = getIntent().getStringExtra("tutorName");

        tvTutorName = findViewById(R.id.tvTutorName);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        tvBio = findViewById(R.id.tvBio);
        btnMessage = findViewById(R.id.btnMessage);
        btnBack = findViewById(R.id.btnBack);
        lvReviews = findViewById(R.id.lvReviews);

        tvTutorName.setText(tutorName);

        btnBack.setOnClickListener(v -> finish());

        btnMessage.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("otherUid", tutorUid);
            i.putExtra("otherName", tutorName);
            startActivity(i);
        });

        reviewsList = new ArrayList<>();
        adapter = new ReviewListAdapter(this, reviewsList);
        lvReviews.setAdapter(adapter);

        loadProfileData();
    }

    private void loadProfileData() {
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("users").child(tutorUid);
        DatabaseReference dbReviews = FirebaseDatabase.getInstance().getReference("reviews").child(tutorUid);

        dbUser.child("bio").get().addOnSuccessListener(snapshot -> {
            String bio = snapshot.getValue(String.class);
            if (bio == null || bio.isEmpty()) {
                tvBio.setText("This tutor has not written a bio yet.");
            } else {
                tvBio.setText(bio);
            }
        });

        dbReviews.get().addOnSuccessListener(snapshot -> {
            reviewsList.clear();
            double total = 0;
            int count = 0;

            for (DataSnapshot snap : snapshot.getChildren()) {
                Double rating = snap.child("rating").getValue(Double.class);
                String comment = snap.child("comment").getValue(String.class);

                if (rating != null) {
                    total += rating;
                    count++;
                    reviewsList.add(new ReviewListAdapter.ReviewItem(rating.floatValue(), comment));
                }
            }

            adapter.notifyDataSetChanged();

            if (count > 0) {
                double avg = total / count;
                tvAvgRating.setText(String.format("%.1f ★ (%d reviews)", avg, count));
            } else {
                tvAvgRating.setText("No ratings yet");
            }
        });
    }
}