package com.example.all_in_one_bazaar.ui.client.adapter;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.all_in_one_bazaar.R;
import com.example.all_in_one_bazaar.model.Product;
import com.example.all_in_one_bazaar.ui.client.product.ProductDetailActivity;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final Context context;
    private final List<Product> bannerList;

    public BannerAdapter(Context context, List<Product> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Product product = bannerList.get(position);

        holder.txtBannerTitle.setText(product.getName() != null ? product.getName() : "Special Offer");

        // ── 🛠️ FIX: રિયલ-ટાઇમ ડિસ્કાઉન્ટ બેજ ગણતરી અને ડિસ્પ્લે લોજિક ──
        String sPrice = product.getPrice();
        String oPrice = product.getOriginalPrice();
        if (sPrice != null && oPrice != null && !sPrice.isEmpty() && !oPrice.isEmpty() && !sPrice.equals("null") && !oPrice.equals("null")) {
            try {
                double sp = Double.parseDouble(sPrice);
                double op = Double.parseDouble(oPrice);
                if (op > sp && op > 0) {
                    int discountPercent = (int) (((op - sp) / op) * 100);
                    holder.txtOfferTag.setText(discountPercent + "% OFF"); // ઓટોમેટીક "45% OFF" લખાઈ જશે
                } else {
                    holder.txtOfferTag.setText("MEGA DEAL");
                }
            } catch (Exception e) {
                holder.txtOfferTag.setText("EXCLUSIVE DEAL");
            }
        } else {
            holder.txtOfferTag.setText("SPECIAL DEALS");
        }

        holder.txtBannerPrice.setText("Only ₹" + (product.getPrice() != null ? product.getPrice() : "0") + " \u2192");

        // Base64 Image Decoding with Sampling
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
                    holder.imgBannerProduct.setImageBitmap(decodedByte);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.imgBannerProduct.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("id", product.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
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

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBannerProduct;
        TextView txtOfferTag, txtBannerTitle, txtBannerPrice;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBannerProduct = itemView.findViewById(R.id.imgBannerProduct);
            txtOfferTag = itemView.findViewById(R.id.txtOfferTag);
            txtBannerTitle = itemView.findViewById(R.id.txtBannerTitle);
            txtBannerPrice = itemView.findViewById(R.id.txtBannerPrice); // આ બટનની અંદરનું ટેક્સ્ટ આઈડી છે
        }
    }
}