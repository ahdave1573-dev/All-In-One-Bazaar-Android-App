package com.example.all_in_one_bazaar.ui.auth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    public static class OnboardingItem {
        public final String title;
        public final String description;
        public final int imageRes; // drawable resource id

        public OnboardingItem(String title, String description, int imageRes) {
            this.title = title;
            this.description = description;
            this.imageRes = imageRes;
        }
    }

    private final List<OnboardingItem> items;
    private final Context context;

    public OnboardingAdapter(Context context, List<OnboardingItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items.get(position);
        holder.title.setText(item.title);
        holder.description.setText(item.description);
        if (item.imageRes != 0) {
            holder.image.setImageResource(item.imageRes);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView image;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtOnboardingTitle);
            description = itemView.findViewById(R.id.txtOnboardingDesc);
            image = itemView.findViewById(R.id.imgOnboarding);
        }
    }
}
