package com.example.all_in_one_bazaar.ui.admin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.all_in_one_bazaar.R;

import java.util.List;
import java.util.Map;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, String>> userList;

    public AdminUserAdapter(Context context, List<Map<String, String>> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> user = userList.get(position);

        String name = user.get("name");
        String email = user.get("email");
        String provider = user.get("provider");
        String role = user.get("role");
        String image = user.get("image");

        holder.txtName.setText(name != null && !name.isEmpty() ? name : "Unknown User");
        holder.txtEmail.setText(email != null && !email.isEmpty() ? email : "No email");

        // ==========================================
        // Smart Logic for Login Provider
        // ==========================================
        boolean isGoogleLogin = false;

        // જો ડેટાબેઝમાં Google લખ્યું હોય
        if (provider != null && provider.toLowerCase().contains("google")) {
            isGoogleLogin = true;
        }
        // અથવા જો ફોટાની લિંક Google ની હોય (Google Login વાળાના ફોટામાં આ લિંક હોય જ છે)
        else if (image != null && image.contains("googleusercontent.com")) {
            isGoogleLogin = true;
        }

        if (isGoogleLogin) {
            holder.txtProvider.setText("Login via: Google");
            holder.txtProvider.setTextColor(Color.parseColor("#4285F4")); // Google Blue Color for better UI
        } else {
            holder.txtProvider.setText("Login via: Email/Password");
            holder.txtProvider.setTextColor(Color.parseColor("#B58B84")); // Default Peach Color
        }

        // ==========================================
        // Role badge with Theme Colors
        // ==========================================
        if (role != null && role.equalsIgnoreCase("admin")) {
            holder.txtRole.setText("ADMIN");
            holder.cardRoleBadge.setCardBackgroundColor(Color.parseColor("#FADCD9")); // Light Red
            holder.txtRole.setTextColor(Color.parseColor("#C70039")); // Dark Crimson
        } else {
            holder.txtRole.setText("USER");
            holder.cardRoleBadge.setCardBackgroundColor(Color.parseColor("#F0D3CE")); // Peach
            holder.txtRole.setTextColor(Color.parseColor("#900C3F")); // Ruby Red
        }

        // ==========================================
        // User avatar Loading
        // ==========================================
        try {
            if (image != null && !image.isEmpty() && !image.equals("null")) {
                if (image.startsWith("http")) {
                    Glide.with(context).load(image)
                            .placeholder(android.R.drawable.ic_menu_myplaces)
                            .into(holder.imgAvatar);
                } else {
                    byte[] imageBytes = Base64.decode(image, Base64.DEFAULT);
                    Glide.with(context).asBitmap().load(imageBytes)
                            .placeholder(android.R.drawable.ic_menu_myplaces)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.imgAvatar);
                }
            } else {
                holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
        } catch (Exception e) {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName, txtEmail, txtProvider, txtRole;
        CardView cardRoleBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtName = itemView.findViewById(R.id.txtUserName);
            txtEmail = itemView.findViewById(R.id.txtUserEmail);
            txtProvider = itemView.findViewById(R.id.txtUserProvider);
            txtRole = itemView.findViewById(R.id.txtUserRole);
            cardRoleBadge = itemView.findViewById(R.id.cardRoleBadge);
        }
    }
}