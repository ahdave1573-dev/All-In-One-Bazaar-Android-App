package com.example.all_in_one_bazaar.ui.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;

import java.util.List;
import java.util.Map;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> list;

    public AdminReviewAdapter(Context context, List<Map<String, Object>> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> currentItem = list.get(position);

        // Activity માંથી userName, comment અને rating મેળવ્યા
        String userName = String.valueOf(currentItem.get("userName"));
        String uId = String.valueOf(currentItem.get("userId"));
        String commentStr = String.valueOf(currentItem.get("comment"));
        String ratingStr = String.valueOf(currentItem.get("rating"));

        // 🛠️ યુઝરનું નામ અથવા લોડિંગ સ્ટેટસ સેટ કરવા માટેનું ફિક્સ લોજિક
        if (userName != null && !userName.isEmpty() && !userName.equals("null")) {
            holder.txtUser.setText(userName); // સાચું નામ અથવા "Loading..." બતાવશે
        } else {
            // જો નામ બિલકુલ ન મળે તો સેફ્ટી માટે શોર્ટ UID બતાવશે
            if (uId != null && uId.length() > 8) {
                holder.txtUser.setText(uId.substring(0, 8) + "...");
            } else {
                holder.txtUser.setText(uId);
            }
        }

        // Comment સેટ કરો
        if (commentStr == null || commentStr.isEmpty() || commentStr.equals("null")) {
            holder.txtComment.setText("No written review text.");
        } else {
            holder.txtComment.setText(commentStr);
        }

        // Stars સેટ કરો
        try {
            float stars = Float.parseFloat(ratingStr);
            holder.rowRatingBar.setRating(stars);
        } catch (Exception e) {
            holder.rowRatingBar.setRating(0);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtComment;
        RatingBar rowRatingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txtReviewUser);
            txtComment = itemView.findViewById(R.id.txtReviewComment);
            rowRatingBar = itemView.findViewById(R.id.rowRatingBar);
        }
    }
}