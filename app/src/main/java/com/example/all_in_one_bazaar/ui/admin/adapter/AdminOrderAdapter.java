package com.example.all_in_one_bazaar.ui.admin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Order;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    Context context;
    List<Order> orderList;

    public AdminOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= orderList.size()) return;
        Order order = orderList.get(position);
        if (order == null) return;

        holder.txtOrderId.setText("Order ID: " + (order.getOrderId() != null ? order.getOrderId() : "N/A"));
        holder.txtAmount.setText("Total: ₹ " + (order.getTotalAmount() != null ? order.getTotalAmount() : "0"));

        String status = (order.getStatus() != null) ? order.getStatus() : "Pending";
        holder.txtStatus.setText(status);

        if ("Pending".equals(status)) {
            holder.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#FADCD9"));
            holder.txtStatus.setTextColor(Color.parseColor("#C70039"));
        } else if ("Shipped".equals(status)) {
            holder.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#F0D3CE"));
            holder.txtStatus.setTextColor(Color.parseColor("#900C3F"));
        } else if ("Delivered".equals(status)) {
            holder.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#E3EBE6"));
            holder.txtStatus.setTextColor(Color.parseColor("#008955"));
        } else {
            holder.cardStatusBadge.setCardBackgroundColor(Color.parseColor("#F9EAE7"));
            holder.txtStatus.setTextColor(Color.parseColor("#B58B84"));
        }

        holder.btnChangeStatus.setOnClickListener(v -> showStatusDialog(order));
    }

    private void showStatusDialog(Order order) {
        String[] statuses = {"Pending", "Shipped", "Delivered", "Cancelled"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Status");
        builder.setItems(statuses, (dialog, which) -> {
            String selectedStatus = statuses[which];
            updateOrderStatus(order, selectedStatus);
        });
        builder.show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        String oldStatus = order.getStatus();
        if (newStatus.equals(oldStatus)) return;

        FirebaseDatabase.getInstance().getReference("Orders")
                .child(order.getOrderId())
                .child("status")
                .setValue(newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Status Updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        order.setStatus(newStatus);
                        notifyDataSetChanged();

                        // 🔥 લોજિક ટ્રિગર્સ
                        if ("Delivered".equals(newStatus)) {
                            decreaseProductStock(order.getProductId(), order.getQuantity());
                            sendDeliveryNotificationToUser(order);
                        } else if ("Cancelled".equals(newStatus)) {
                            // 🛠️ FIX: કેન્સલ થવા પર સ્ટોક વધારશે અને યુઝરને એલર્ટ મોકલશે
                            increaseProductStock(order.getProductId(), order.getQuantity());
                            sendCancelNotificationToUser(order);
                        }
                    } else {
                        Toast.makeText(context, "Failed Update", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─── યુઝરને ડિલિવરીનું નોટિફિકેશન મોકલવા માટેનું ફંક્શન ───
    private void sendDeliveryNotificationToUser(Order order) {
        if (order.getUserId() == null || order.getUserId().equals("N/A")) return;

        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(order.getUserId());
        String notifId = notifRef.push().getKey();

        String timestamp = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());

        HashMap<String, Object> notifMap = new HashMap<>();
        notifMap.put("id", notifId);
        notifMap.put("title", "Order Delivered! 📦");
        notifMap.put("message", "Your order #" + order.getOrderId() + " has been successfully delivered. Enjoy your purchase!");
        notifMap.put("type", "order_delivered");
        notifMap.put("timestamp", timestamp);

        if (notifId != null) {
            notifRef.child(notifId).setValue(notifMap);
        }
    }

    // ─── 🛠️ FIX: યુઝરને ઓર્ડર કેન્સલેશનનું નોટિફિકેશન મોકલવા માટેનું નવું ફંક્શન ───
    private void sendCancelNotificationToUser(Order order) {
        if (order.getUserId() == null || order.getUserId().equals("N/A")) return;

        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("Notifications").child(order.getUserId());
        String notifId = notifRef.push().getKey();

        String timestamp = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());

        HashMap<String, Object> notifMap = new HashMap<>();
        notifMap.put("id", notifId);
        notifMap.put("title", "Order Cancelled ❌");
        notifMap.put("message", "Your order #" + order.getOrderId() + " has been cancelled. If any amount was deducted, it will be refunded soon.");
        notifMap.put("type", "order_cancelled");
        notifMap.put("timestamp", timestamp);

        if (notifId != null) {
            notifRef.child(notifId).setValue(notifMap);
        }
    }

    private void decreaseProductStock(String productId, int orderQty) {
        if (productId == null || productId.isEmpty()) return;
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int currentQty = 0;
                    if (snapshot.child("quantity").getValue() != null) {
                        try {
                            currentQty = Integer.parseInt(String.valueOf(snapshot.child("quantity").getValue()));
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    int newQty = currentQty - orderQty;
                    if (newQty < 0) newQty = 0;

                    productRef.child("quantity").setValue(newQty);
                    if (newQty == 0) {
                        productRef.child("stockStatus").setValue("Out of Stock");
                    } else {
                        productRef.child("stockStatus").setValue("In Stock");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    // ─── 🛠️ FIX: ઓર્ડર કેન્સલ થતાં સ્ટોક પાછો પ્લસ કરવાનું નવું ફંક્શન ───
    private void increaseProductStock(String productId, int orderQty) {
        if (productId == null || productId.isEmpty()) return;
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int currentQty = 0;
                    if (snapshot.child("quantity").getValue() != null) {
                        try {
                            currentQty = Integer.parseInt(String.valueOf(snapshot.child("quantity").getValue()));
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                    int newQty = currentQty + orderQty;

                    productRef.child("quantity").setValue(newQty);
                    if (newQty > 0) {
                        productRef.child("stockStatus").setValue("In Stock");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtAmount, txtStatus;
        Button btnChangeStatus;
        CardView cardStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtAdminOrderId);
            txtAmount = itemView.findViewById(R.id.txtAdminOrderAmount);
            txtStatus = itemView.findViewById(R.id.txtAdminOrderStatus);
            btnChangeStatus = itemView.findViewById(R.id.btnChangeStatus);
            cardStatusBadge = itemView.findViewById(R.id.cardStatusBadge);
        }
    }
}