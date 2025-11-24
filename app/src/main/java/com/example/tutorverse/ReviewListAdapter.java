package com.example.tutorverse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class ReviewListAdapter extends ArrayAdapter<ReviewListAdapter.ReviewItem> {

    public static class ReviewItem {
        public float rating;
        public String comment;

        public ReviewItem(float rating, String comment) {
            this.rating = rating;
            this.comment = comment;
        }
    }

    public ReviewListAdapter(@NonNull Context context, ArrayList<ReviewItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_review_item, parent, false);
        }

        ReviewItem item = getItem(position);

        RatingBar rbReview = convertView.findViewById(R.id.rbReview);
        TextView tvComment = convertView.findViewById(R.id.tvComment);

        if (item != null) {
            rbReview.setRating(item.rating);
            tvComment.setText(item.comment);
        }

        return convertView;
    }
}