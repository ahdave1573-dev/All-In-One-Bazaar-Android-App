package com.example.all_in_one_bazaar.ui.client.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView; 

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.client.product.ProductDetailActivity;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    Context context;
    List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setFilteredList(List<Product> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        // Bounds check
        if (position < 0 || position >= productList.size()) return;

        Product product = productList.get(position);
        if (product == null) return;

        // Null-safe text display
        holder.txtName.setText(product.getName() != null ? product.getName() : "Unnamed");
        holder.txtPrice.setText("₹ " + (product.getPrice() != null ? product.getPrice() : "0"));
        holder.txtCategory.setText(product.getCategory() != null ? product.getCategory() : "N/A");

        // Rating Logic
        if (product.getRating() != null && !product.getRating().isEmpty()) {
            holder.txtRating.setText(product.getRating() + " ★");
        } else {
            holder.txtRating.setText("0.0 ★");
        }

        // Discount Calculation Logic
        try {
            if (product.getOriginalPrice() != null && product.getPrice() != null) {
                double original = Double.parseDouble(product.getOriginalPrice());
                double selling = Double.parseDouble(product.getPrice());
                if (original > selling) {
                    int percent = (int) (((original - selling) / original) * 100);
                    holder.txtDiscountBig.setText(percent + "% OFF");
                } else {
                    holder.txtDiscountBig.setText("HOT DEAL");
                }
            } else {
                holder.txtDiscountBig.setText("SALE");
            }
        } catch (Exception e) {
            holder.txtDiscountBig.setText("SALE");
        }
        holder.imgProduct.setVisibility(View.GONE); // Always hide image as requested
        holder.txtDiscountBig.setVisibility(View.VISIBLE);

        // 🔥 Stock Status Fix: String to int comparison
        String status = product.getStockStatus();
        int qty = 0;
        try {
            if (product.getQuantity() != null) {
                // String ને Number માં કન્વર્ટ કરો જેથી સરખામણી થઈ શકે
                qty = Integer.parseInt(product.getQuantity());
            }
        } catch (NumberFormatException e) {
            qty = 0;
        }

        if ("Out of Stock".equalsIgnoreCase(status) || qty <= 0) {
            holder.txtStockStatus.setText("Out of Stock");
            holder.txtStockStatus.setTextColor(Color.RED);
        } else {
            holder.txtStockStatus.setText("In Stock");
            holder.txtStockStatus.setTextColor(Color.parseColor("#4CAF50"));
        }

        // Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (product.getId() == null || product.getId().isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "Error: Product ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("id", product.getId());
            holder.itemView.getContext().startActivity(intent);
        });
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

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice, txtCategory, txtRating, txtStockStatus, txtDiscountBig;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtStockStatus = itemView.findViewById(R.id.txtStockStatus);
            txtDiscountBig = itemView.findViewById(R.id.txtDiscountBig);
        }
    }
}