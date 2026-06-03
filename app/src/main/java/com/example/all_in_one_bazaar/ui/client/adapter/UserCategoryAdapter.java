package com.example.all_in_one_bazaar.ui.client.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Category;

import java.util.List;

public class UserCategoryAdapter
        extends RecyclerView.Adapter<UserCategoryAdapter.ViewHolder> {

    private final Context  context;
    private final List<Category> list;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public UserCategoryAdapter(Context context,
                               List<Category> list,
                               OnCategoryClickListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_user_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = list.get(position);
        holder.txtCatName.setText(category.getName());

        String imageStr = category.getImage();

        if (imageStr != null && !imageStr.isEmpty() && !"null".equals(imageStr)) {
            try {
                // Strip data URI prefix if present
                if (imageStr.contains(",")) {
                    imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                }
                imageStr = imageStr.trim();

                byte[]  decoded = Base64.decode(imageStr, Base64.DEFAULT);
                Bitmap  bitmap  = BitmapFactory.decodeByteArray(
                        decoded, 0, decoded.length);

                if (bitmap != null) {
                    // Clear any tint — show the real photo
                    holder.imgCatIcon.clearColorFilter();
                    holder.imgCatIcon.setImageTintList(null);
                    holder.imgCatIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.imgCatIcon.setImageBitmap(bitmap);
                } else {
                    setPlaceholder(holder);
                }
            } catch (Exception e) {
                e.printStackTrace();
                setPlaceholder(holder);
            }
        } else {
            setPlaceholder(holder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(category.getName());
        });
    }

    /** Show a themed placeholder icon when no image is available. */
    private void setPlaceholder(ViewHolder holder) {
        holder.imgCatIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.imgCatIcon.setImageResource(android.R.drawable.ic_menu_gallery);
        // Ocean Calm: use ocean_secondary tint instead of hardcoded purple
        holder.imgCatIcon.setColorFilter(
                context.getResources().getColor(
                        R.color.ocean_secondary,
                        context.getTheme()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    // ── ViewHolder ────────────────────────────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCatIcon;
        TextView  txtCatName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCatIcon = itemView.findViewById(R.id.imgCatIcon);
            txtCatName = itemView.findViewById(R.id.txtCatName);
        }
    }
}
