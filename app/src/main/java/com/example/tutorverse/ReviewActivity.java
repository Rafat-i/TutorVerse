package com.example.tutorverse;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    TextView tvTutorName;
    RatingBar ratingBar;
    EditText edComment;
    Button btnSubmitReview;

    String tutorUid, tutorName, bookingKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        // Get data passed from the Bookings page
        tutorUid = getIntent().getStringExtra("tutorUid");
        tutorName = getIntent().getStringExtra("tutorName");
        bookingKey = getIntent().getStringExtra("bookingKey");

        tvTutorName = findViewById(R.id.tvTutorName);
        ratingBar = findViewById(R.id.ratingBar);
        edComment = findViewById(R.id.edComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);

        if (tutorName != null) {
            tvTutorName.setText("Tutor: " + tutorName);
        }

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = edComment.getText().toString().trim();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please write a brief comment", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbReviews = FirebaseDatabase.getInstance().getReference("reviews");
        DatabaseReference dbBookings = FirebaseDatabase.getInstance().getReference("bookings");

        String reviewId = dbReviews.push().getKey();
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("studentUid", studentUid);
        reviewData.put("timestamp", System.currentTimeMillis());

        if (reviewId != null) {
            dbReviews.child(tutorUid).child(reviewId).setValue(reviewData)
                    .addOnSuccessListener(aVoid -> {

                        if (bookingKey != null) {
                            dbBookings.child(tutorUid).child(bookingKey).removeValue();
                        }

                        Toast.makeText(this, "Review Submitted!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}