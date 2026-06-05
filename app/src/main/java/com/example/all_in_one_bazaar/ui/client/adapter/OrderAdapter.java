package com.example.all_in_one_bazaar.ui.client.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Order;
import com.example.all_in_one_bazaar.ui.client.product.RateProductActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    Context context;
    List<Order> orderList;
    String currentUserId;

    // ડિલિવરી ચાર્જ કોન્સ્ટન્ટ
    private static final double DELIVERY_CHARGE = 49.0;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            this.currentUserId = "";
        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        try {
            Order order = orderList.get(position);

            holder.txtOrderId.setText("Order #" + (order.getOrderId() != null ? order.getOrderId() : "N/A"));

            String dateText = (order.getDate() != null) ? order.getDate() : "N/A";
            holder.txtDate.setText("Date: " + dateText);

            String status = (order.getStatus() != null) ? order.getStatus() : "Pending";
            holder.txtStatus.setText(status);

            // 🔥 FIX: કન્ડિશનલ ડિલિવરી ચાર્જ અને ફાઇનલ અમાઉન્ટ ગણતરી લોજિક
            holder.txtShippingLabel.setText("Delivery Charges"); // "Shipping Info" બદલીને સેટ કર્યું

            if (order.getTotalAmount() != null) {
                try {
                    double productAmount = Double.parseDouble(order.getTotalAmount());
                    double finalAmount;

                    // જો પ્રોડક્ટનું બિલ 5000 કે તેથી ઓછું હોય, તો ₹49 ચાર્જ લાગશે
                    if (productAmount <= 5000.0) {
                        finalAmount = productAmount + DELIVERY_CHARGE;
                        holder.txtDeliveryCharge.setText("₹ 49.00");
                        holder.txtDeliveryCharge.setTextColor(Color.parseColor("#F44336")); // Red color for charge
                    } else {
                        // 5000 થી ઉપર ફ્રી ડિલિવરી
                        finalAmount = productAmount;
                        holder.txtDeliveryCharge.setText("FREE");
                        holder.txtDeliveryCharge.setTextColor(Color.parseColor("#4CAF50")); // Green color for FREE
                    }

                    // ફાઇનલ અમાઉન્ટ સેટ કરો
                    holder.txtTotal.setText(String.format(Locale.getDefault(), "₹ %.2f", finalAmount));

                } catch (Exception e) {
                    holder.txtTotal.setText("₹ " + order.getTotalAmount());
                    holder.txtDeliveryCharge.setText("FREE");
                }
            } else {
                holder.txtTotal.setText("₹ 0");
                holder.txtDeliveryCharge.setText("FREE");
            }

            // ડિફોલ્ટ રેટિંગ સ્ટેટ
            holder.btnRate.setVisibility(View.GONE);
            holder.txtRateStatus.setVisibility(View.GONE);

            // Status પ્રમાણે કલર બદલવા
            if ("Delivered".equalsIgnoreCase(status)) {
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
                holder.txtStatus.setTextColor(Color.parseColor("#4CAF50")); // Green

                checkIfAlreadyRated(order.getOrderId(), order.getProductId(), holder.btnRate, holder.txtRateStatus);

                holder.btnRate.setOnClickListener(v -> {
                    if (order.getProductId() != null && !order.getProductId().isEmpty()) {
                        Intent intent = new Intent(context, RateProductActivity.class);
                        intent.putExtra("productId", order.getProductId());
                        intent.putExtra("orderId", order.getOrderId());
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Error: Product ID not found", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if ("Cancelled".equalsIgnoreCase(status)) {
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // Light Red
                holder.txtStatus.setTextColor(Color.parseColor("#F44336")); // Red
            } else {
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FEF3E2"));
                holder.txtStatus.setTextColor(Color.parseColor("#854F0B"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkIfAlreadyRated(String orderId, String productId, CardView btnRate, TextView txtRateStatus) {
        if (currentUserId.isEmpty() || productId == null || productId.isEmpty()) {
            btnRate.setVisibility(View.VISIBLE);
            txtRateStatus.setVisibility(View.GONE);
            return;
        }

        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(productId);
        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasRated = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String dbUserId = ds.child("userId").getValue(String.class);
                    String dbOrderId = ds.child("orderId").getValue(String.class);

                    if (dbUserId != null && dbUserId.equals(currentUserId)) {
                        if (dbOrderId != null && dbOrderId.equals(orderId)) {
                            hasRated = true;
                            break;
                        }
                    }
                }

                if (hasRated) {
                    btnRate.setVisibility(View.GONE);
                    txtRateStatus.setVisibility(View.VISIBLE);
                    txtRateStatus.setText("Rated 🌟");
                } else {
                    btnRate.setVisibility(View.VISIBLE);
                    txtRateStatus.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnRate.setVisibility(View.VISIBLE);
                txtRateStatus.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtDate, txtTotal, txtStatus, txtRateStatus, txtDeliveryCharge, txtShippingLabel;
        CardView btnRate, cardStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtDate = itemView.findViewById(R.id.txtOrderDate);
            txtTotal = itemView.findViewById(R.id.txtOrderTotal);
            txtStatus = itemView.findViewById(R.id.txtOrderStatus);
            cardStatus = itemView.findViewById(R.id.cardStatus);
            btnRate = itemView.findViewById(R.id.btnRateProduct);
            txtRateStatus = itemView.findViewById(R.id.txtRateStatus);
            txtDeliveryCharge = itemView.findViewById(R.id.txtOrderDeliveryCharge);

            // 🔥 XML માં "Shipping Info" વાળા TextView ને ઓળખવા માટે આ આઈડી એસાઈન કર્યું
            txtShippingLabel = itemView.findViewById(R.id.txtShippingLabel);
        }
    }
}