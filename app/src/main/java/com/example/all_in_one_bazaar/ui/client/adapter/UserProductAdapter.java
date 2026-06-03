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
import com.example.all_in_one_bazaar.ui.client.product.ProductDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class UserProductAdapter extends RecyclerView.Adapter<UserProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> list;

    public UserProductAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    public void setFilteredList(List<Product> filteredList) {
        this.list.clear();
        this.list.addAll(filteredList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);

        holder.txtProductName.setText(product.getName() != null && !product.getName().equals("null") ? product.getName() : "Unknown Product");
        holder.txtProductPrice.setText("₹" + (product.getPrice() != null && !product.getPrice().equals("null") ? product.getPrice() : "0"));

        // Short Description સેટ કરો
        String shortDesc = product.getDescription();
        if (shortDesc != null && !shortDesc.trim().isEmpty() && !shortDesc.equals("null")) {
            holder.txtProductShortDesc.setText(shortDesc);
            holder.txtProductShortDesc.setVisibility(View.VISIBLE);
        } else {
            holder.txtProductShortDesc.setVisibility(View.GONE);
        }

        String cat = product.getCategory();
        if (cat != null && !cat.equalsIgnoreCase("null") && !cat.trim().isEmpty()) {
            holder.txtProductCategory.setText(cat);
        } else {
            holder.txtProductCategory.setText("Other");
        }

        String rat = product.getRating();
        if (rat != null && !rat.equalsIgnoreCase("null") && !rat.trim().isEmpty()) {
            if(rat.contains("★")) {
                holder.txtProductRating.setText(rat);
            } else {
                holder.txtProductRating.setText(rat + " ★");
            }
        } else {
            holder.txtProductRating.setText("0.0 ★");
        }

        String stock = product.getStockStatus();
        int quantity = 0;
        try {
            String qStr = product.getQuantity();
            if (qStr != null && !qStr.isEmpty() && !qStr.equals("null")) {
                quantity = Integer.parseInt(qStr);
            }
        } catch (Exception e) {
            quantity = 0;
        }

        boolean isInStock = false;
        if (stock != null && !stock.equalsIgnoreCase("null") && !stock.trim().isEmpty()) {
            String cleanStock = stock.trim().toLowerCase();
            if (cleanStock.equals("in stock") || cleanStock.equals("available")) {
                isInStock = true;
            }
        }

        if (quantity > 0) isInStock = true;

        if (isInStock) {
            holder.txtProductStock.setText("In Stock");
            holder.txtProductStock.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.txtProductStock.setText("Out of Stock");
            holder.txtProductStock.setTextColor(Color.parseColor("#F44336")); // Red
        }

        String sPrice = product.getPrice();
        String oPrice = product.getOriginalPrice();
        if (sPrice != null && oPrice != null && !sPrice.isEmpty() && !oPrice.isEmpty() && !sPrice.equals("null") && !oPrice.equals("null")) {
            try {
                double sp = Double.parseDouble(sPrice);
                double op = Double.parseDouble(oPrice);
                if (op > sp && op > 0) {
                    int discountPercent = (int) (((op - sp) / op) * 100);
                    holder.txtProductDiscount.setText(discountPercent + "% OFF");
                    holder.cardDiscount.setVisibility(View.VISIBLE);
                } else {
                    holder.cardDiscount.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                holder.cardDiscount.setVisibility(View.GONE);
            }
        } else {
            holder.cardDiscount.setVisibility(View.GONE);
        }

        String imageStr = product.getImageUrl();
        if (imageStr != null && !imageStr.isEmpty() && !imageStr.equals("null")) {
            try {
                if (imageStr.contains(",")) {
                    imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                }
                byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
                opts.inSampleSize = calculateInSampleSize(opts, 250, 250);
                opts.inJustDecodeBounds = false;

                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, opts);
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

        final int finalQuantity = quantity;
        holder.btnAddToCart.setOnClickListener(v -> {
            boolean canAddToCart = false;
            String currentStock = product.getStockStatus();

            if (currentStock != null && !currentStock.equalsIgnoreCase("null") && !currentStock.trim().isEmpty()) {
                String cleanStock = currentStock.trim().toLowerCase();
                if (cleanStock.equals("in stock") || cleanStock.equals("available")) {
                    canAddToCart = true;
                }
            }
            if (finalQuantity > 0) canAddToCart = true;

            if (!canAddToCart) {
                Toast.makeText(context, "Product is Out of Stock!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(context, "Please Login first!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(user.getUid());
            HashMap<String, Object> cartMap = new HashMap<>();
            cartMap.put("productId", product.getId() != null ? product.getId() : "");
            cartMap.put("name", product.getName() != null ? product.getName() : "");
            cartMap.put("price", product.getPrice() != null ? product.getPrice() : "0");
            cartMap.put("image", product.getImageUrl() != null ? product.getImageUrl() : "");
            cartMap.put("quantity", "1");

            cartRef.child(product.getId() != null ? product.getId() : String.valueOf(System.currentTimeMillis())).setValue(cartMap)
                    .addOnSuccessListener(task -> Toast.makeText(context, product.getName() + " Added to Cart! 🛒", Toast.LENGTH_SHORT).show());
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("id", product.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtProductShortDesc, txtProductPrice, txtProductCategory, txtProductStock, txtProductRating, txtProductDiscount;
        CardView btnAddToCart, cardDiscount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductShortDesc = itemView.findViewById(R.id.txtProductShortDesc);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductCategory = itemView.findViewById(R.id.txtProductCategory);

            // 🛠️ FIXED LINE: હવે સાચો વેરિએબલ txtProductStock અહીં લિંક થઈ ગયો છે
            txtProductStock = itemView.findViewById(R.id.txtProductStock);

            txtProductRating = itemView.findViewById(R.id.txtProductRating);
            txtProductDiscount = itemView.findViewById(R.id.txtProductDiscount);
            cardDiscount = itemView.findViewById(R.id.cardDiscount);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}