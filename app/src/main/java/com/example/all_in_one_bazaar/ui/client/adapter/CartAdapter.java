package com.example.all_in_one_bazaar.ui.client.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase; 

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    Context context;
    List<Product> cartList;
    DatabaseReference cartRef;
    String userId;

    public CartAdapter(Context context, List<Product> cartList) {
        this.context = context;
        this.cartList = cartList;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
        }   
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        if (position < 0 || position >= cartList.size()) return;

        Product product = cartList.get(position);
        if (product == null) return;

        holder.txtName.setText(product.getName() != null ? product.getName() : "Unnamed");
        holder.txtPrice.setText("₹ " + (product.getPrice() != null ? product.getPrice() : "0"));
        holder.txtQty.setText(product.getQuantity());

        // Image loading logic
        String imageStr = product.getImageUrl();
        if (imageStr != null && !imageStr.isEmpty() && !imageStr.equals("null")) {
            try {
                if (imageStr.startsWith("http")) {
                    Glide.with(holder.itemView.getContext()).load(imageStr).into(holder.imgProduct);
                } else {
                    byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.imgProduct.setImageBitmap(decodedByte);
                }
            } catch (Exception e) {
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // ➕ PLUS BUTTON LOGIC
        holder.btnPlus.setOnClickListener(v -> {
            if (product.getId() != null) {
                try {
                    int currentQty = Integer.parseInt(product.getQuantity());
                    int nextQty = currentQty + 1;

                    FirebaseDatabase.getInstance().getReference("products")
                            .child(product.getId())
                            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Object qtyObj = snapshot.child("quantity").getValue();
                                        int availableStock = (qtyObj != null) ? Integer.parseInt(String.valueOf(qtyObj)) : 0;

                                        if (nextQty <= availableStock) {
                                            updateQuantityInFirebase(product.getId(), nextQty);
                                        } else {
                                            Toast.makeText(context, "Stock Limit: " + availableStock, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                            });
                } catch (Exception e) {
                    Log.e("CartAdapter", "Qty conversion error", e);
                }
            }
        });

        // ➖ MINUS BUTTON LOGIC
        holder.btnMinus.setOnClickListener(v -> {
            if (product.getId() == null) return;
            try {
                int currentQty = Integer.parseInt(product.getQuantity());
                if (currentQty > 1) {
                    updateQuantityInFirebase(product.getId(), currentQty - 1);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                    deleteItemFromFirebase(product.getId());
                }
            } catch (Exception e) {
                Log.e("CartAdapter", "Qty conversion error", e);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (product.getId() != null) deleteItemFromFirebase(product.getId());
        });
    }

    private void updateQuantityInFirebase(String productId, int newQty) {
        if (cartRef != null && productId != null) {
            cartRef.child(productId).child("quantity").setValue(String.valueOf(newQty));
        }
    }

    private void deleteItemFromFirebase(String productId) {
        if (cartRef != null && productId != null) {
            cartRef.child(productId).removeValue();
        }
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnPlus, btnMinus, btnDelete;
        TextView txtName, txtPrice, txtQty;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCartProduct);
            txtName = itemView.findViewById(R.id.txtCartName);
            txtPrice = itemView.findViewById(R.id.txtCartPrice);
            txtQty = itemView.findViewById(R.id.txtCartQty);
            btnPlus = itemView.findViewById(R.id.btnCartPlus);
            btnMinus = itemView.findViewById(R.id.btnCartMinus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}