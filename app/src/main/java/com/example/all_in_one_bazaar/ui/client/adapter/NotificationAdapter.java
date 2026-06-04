package com.example.all_in_one_bazaar.ui.client.adapter; // 🛠️ FIX: પેકેજ નામ સ્મોલ અક્ષરોમાં કર્યું

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    Context context;
    List<Notification> list;
    private final OnNotificationLongClickListener longClickListener; // 🛠️ નવું લિસનર ઉમેર્યું

    // 🛠️ સિંગલ ડીલીટ ઇવેન્ટ પકડવા માટે ઇન્ટરફેસ બનાવ્યો
    public interface OnNotificationLongClickListener {
        void onNotificationLongClick(Notification notification);
    }

    // 🛠️ કન્સ્ટ્રક્ટરમાં લિસનર પાસ કર્યું જેથી એક્ટિવિટીમાં મેસેજ મોકલી શકાય
    public NotificationAdapter(Context context, List<Notification> list, OnNotificationLongClickListener longClickListener) {
        this.context = context;
        this.list = list;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification model = list.get(position);

        holder.txtTitle.setText(model.getTitle());
        holder.txtMessage.setText(model.getMessage());
        holder.txtTime.setText(model.getTimestamp() != null ? model.getTimestamp() : "");

        String type = model.getType() != null ? model.getType() : "";

        // નોટિફિકેશનના પ્રકાર પ્રમાણે કલર અને આઈકોન બદલાશે
        if (type.equals("offer")) {
            holder.cardIconBg.setCardBackgroundColor(Color.parseColor("#F5A623"));
            holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_info);
        } else if (type.equals("order_placed")) {
            holder.cardIconBg.setCardBackgroundColor(Color.parseColor("#1A56C4"));
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_agenda);
        } else if (type.equals("order_delivered")) {
            holder.cardIconBg.setCardBackgroundColor(Color.parseColor("#10B981"));
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
        } else {
            holder.cardIconBg.setCardBackgroundColor(Color.parseColor("#4A7FC1"));
            holder.imgIcon.setImageResource(android.R.drawable.ic_popup_reminder);
        }

        // 🛠️ FIX: યુઝર જ્યારે કોઈપણ નોટિફિકેશન પર લોન્ગ પ્રેસ (લાંબુ દબાવી રાખશે) કરશે એટલે ડીલીટ ડાયલોગ ખુલશે
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onNotificationLongClick(model);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardIconBg;
        ImageView imgIcon;
        TextView txtTitle, txtMessage, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardIconBg = itemView.findViewById(R.id.cardIconBg);
            imgIcon = itemView.findViewById(R.id.imgNotifIcon);
            txtTitle = itemView.findViewById(R.id.txtNotifTitle);
            txtMessage = itemView.findViewById(R.id.txtNotifMessage);
            txtTime = itemView.findViewById(R.id.txtNotifTime);
        }
    }
}