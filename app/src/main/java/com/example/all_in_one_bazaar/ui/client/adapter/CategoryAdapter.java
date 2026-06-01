package com.example.all_in_one_bazaar.ui.client.adapter;

import android.app.AlertDialog;
import android.content.Context; 
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Category;
import com.example.all_in_one_bazaar.ui.admin.category.ManageCategoriesActivity;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> list;
    private DatabaseReference categoryRef;

    public CategoryAdapter(Context context, List<Category> list, DatabaseReference categoryRef) {
        this.context = context;
        this.list = list;
        this.categoryRef = categoryRef;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position < 0 || position >= list.size()) return;
        Category category = list.get(position);

        if (category == null) return;

        holder.txtCategoryName.setText(category.getName() != null ? category.getName() : "Unnamed Category");

        // Load Base64 Image Safely
        if (category.getImage() != null && !category.getImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(category.getImage(), Base64.DEFAULT);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // Memory optimization
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
                holder.imgCategoryIcon.setImageBitmap(decodedByte);
            } catch (Exception e) {
                holder.imgCategoryIcon.setImageResource(android.R.drawable.ic_menu_camera);
                e.printStackTrace();
            }
        } else {
            holder.imgCategoryIcon.setImageResource(android.R.drawable.ic_menu_camera);
        }

        // Edit Logic
        holder.btnEdit.setOnClickListener(v -> {
            if (context instanceof ManageCategoriesActivity) {
                ((ManageCategoriesActivity) context).editCategory(category);
            }
        });

        // Delete Logic
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete '" + category.getName() + "'?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (category.getId() != null) {
                            categoryRef.child(category.getId()).removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategoryIcon;
        TextView txtCategoryName;
        CardView btnEdit, btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
