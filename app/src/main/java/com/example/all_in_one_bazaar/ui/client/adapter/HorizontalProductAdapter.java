package com.example.all_in_one_bazaar.ui.client.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import com.example.all_in_one_bazaar.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class HorizontalProductAdapter extends RecyclerView.Adapter<HorizontalProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> list;

    public HorizontalProductAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);

        holder.txtProductName.setText(product.getName() != null ? product.getName() : "N/A");
        holder.txtProductPrice.setText("₹" + product.getPrice());
        holder.txtCategory.setText(product.getCategory() != null ? product.getCategory() : "Bazaar");

        // રેટિંગ સેટઅપ
        String rating = product.getRating();
        holder.txtRating.setText((rating != null && !rating.equals("null") && !rating.isEmpty() ? rating : "0.0") + " ★");

        // ── 🛠️ FIX: લાઈવ સ્ટોક સ્ટેટસ કલર કોડિંગ કન્ડિશન ──
        String stockStatus = product.getStockStatus();
        if (stockStatus != null && stockStatus.equalsIgnoreCase("Out of Stock")) {
            holder.txtStockStatus.setText("Out of Stock");
            holder.txtStockStatus.setTextColor(Color.parseColor("#C70039")); // Red Color
        } else {
            holder.txtStockStatus.setText("In Stock");
            holder.txtStockStatus.setTextColor(Color.parseColor("#008955")); // Green Color
        }

        // Base64 Image Decoding
        String imageStr = product.getImageUrl();
        if (imageStr != null && !imageStr.isEmpty() && !imageStr.equals("null")) {
            try {
                if (imageStr.contains(",")) {
                    imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                }
                byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    holder.imgProduct.setImageBitmap(decodedByte);
                } else {
                    holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e) {
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // કાર્ડ ક્લિક (ડિટેઇલ પેજ)
        holder.itemView.setOnClickListener(v -> {
            try {
                Class<?> detailClass = Class.forName("com.example.all_in_one_bazaar.ui.client.product.ProductDetailActivity");
                Intent intent = new Intent(context, detailClass);
                intent.putExtra("id", product.getId());
                context.startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(context, "Detail page path mismatched", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to Cart ક્લિક લોજિક
        holder.btnAddToCart.setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null) {
                Toast.makeText(context, "Please Login first!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (product.getStockStatus() != null && product.getStockStatus().equalsIgnoreCase("Out of Stock")) {
                Toast.makeText(context, "Product is Out of Stock!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(uid);
            HashMap<String, Object> cartMap = new HashMap<>();
            cartMap.put("productId", product.getId());
            cartMap.put("name", product.getName() != null ? product.getName() : "");
            cartMap.put("price", product.getPrice() != null ? product.getPrice() : "0");
            cartMap.put("image", product.getImageUrl() != null ? product.getImageUrl() : "");
            cartMap.put("quantity", 1);

            cartRef.child(product.getId()).setValue(cartMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, product.getName() + " added to Cart 🛒", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductPrice, txtCategory, txtRating, txtStockStatus; // 🛠️ નવું TextView ઉમેર્યું
        CardView btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgHorProduct);
            txtProductName = itemView.findViewById(R.id.txtHorProductName);
            txtProductPrice = itemView.findViewById(R.id.txtHorProductPrice);
            txtCategory = itemView.findViewById(R.id.txtHorProductCategory);
            txtRating = itemView.findViewById(R.id.txtHorRating);
            txtStockStatus = itemView.findViewById(R.id.txtHorStockStatus); // 🛠️ ID બાઈન્ડ કરી દીધું
            btnAddToCart = itemView.findViewById(R.id.btnHorAddToCart);
        }
    }
}