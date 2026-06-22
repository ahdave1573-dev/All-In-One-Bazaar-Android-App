package com.example.all_in_one_bazaar.ui.admin.adapter;

import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.admin.product.AddProductActivity; // એડિટ માટે આ જ એક્ટિવિટી વાપરી શકાય
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ManageProductAdapter extends RecyclerView.Adapter<ManageProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final DatabaseReference databaseReference;

    public ManageProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("products");
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        if (product != null) {
            String productId = product.getId();

            holder.txtProductName.setText(product.getName());
            holder.txtProductPrice.setText("₹ " + product.getPrice());
            holder.txtStockStatus.setText(product.getStockStatus() != null ? product.getStockStatus() : "In Stock");
            holder.txtRating.setText(product.getRating() != null ? product.getRating() : "0.0");
            holder.txtQuantity.setText(product.getQuantity() != null ? product.getQuantity() : "0");

            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                holder.txtProductCategory.setText(product.getCategory());
                holder.txtProductCategory.setVisibility(View.VISIBLE);
            } else {
                holder.txtProductCategory.setVisibility(View.GONE);
            }

            // Image decoding
            String imageStr = product.getImageUrl();
            if (imageStr != null && !imageStr.isEmpty() && !"null".equals(imageStr)) {
                try {
                    if (imageStr.contains(",")) {
                        imageStr = imageStr.substring(imageStr.indexOf(",") + 1);
                    }
                    byte[] decodedString = Base64.decode(imageStr.trim(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedByte != null) {
                        holder.imgProduct.setImageBitmap(decodedByte);
                    } else {
                        holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // ── 🛠️ FIXED: INCREASE STOCK BUTTON (+) ──
            holder.btnIncreaseStock.setOnClickListener(v -> {
                try {
                    int currentQty = Integer.parseInt(holder.txtQuantity.getText().toString());
                    int newQty = currentQty + 1;

                    // Firebase અપડેટ
                    databaseReference.child(productId).child("quantity").setValue(String.valueOf(newQty))
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to update stock", Toast.LENGTH_SHORT).show());
                } catch (Exception e) { e.printStackTrace(); }
            });

            // ── 🛠️ FIXED: DECREASE STOCK BUTTON (-) ──
            holder.btnDecreaseStock.setOnClickListener(v -> {
                try {
                    int currentQty = Integer.parseInt(holder.txtQuantity.getText().toString());
                    if (currentQty > 0) {
                        int newQty = currentQty - 1;
                        String newStatus = (newQty == 0) ? "Out of Stock" : "In Stock";

                        // Firebase અપડેટ (જથ્થો અને સ્ટેટસ બંને)
                        databaseReference.child(productId).child("quantity").setValue(String.valueOf(newQty));
                        databaseReference.child(productId).child("stockStatus").setValue(newStatus);
                    } else {
                        Toast.makeText(context, "Stock is already 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });

            // ── 🛠️ FIXED: DELETE PRODUCT ACTION ──
            holder.cardDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete this product?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            databaseReference.child(productId).removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            // ── 🛠️ FIXED: EDIT PRODUCT ACTION ──
            holder.cardEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddProductActivity.class);
                intent.putExtra("productId", productId);
                intent.putExtra("isEditMode", true);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnEdit, btnDelete, btnDecreaseStock, btnIncreaseStock;
        CardView cardEdit, cardDelete;
        TextView txtProductName, txtProductCategory, txtProductPrice, txtStockStatus, txtRating, txtQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtStockStatus = itemView.findViewById(R.id.txtStockStatus);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);
            txtProductCategory = itemView.findViewById(R.id.txtProductCategory);

            cardEdit = itemView.findViewById(R.id.cardEdit);
            cardDelete = itemView.findViewById(R.id.cardDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnDecreaseStock = itemView.findViewById(R.id.btnDecreaseStock);
            btnIncreaseStock = itemView.findViewById(R.id.btnIncreaseStock);
        }
    }
}