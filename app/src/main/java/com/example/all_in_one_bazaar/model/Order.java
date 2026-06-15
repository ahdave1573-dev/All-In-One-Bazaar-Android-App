package com.example.all_in_one_bazaar.model;

public class Order {
    private String orderId;
    private String userId;      // ✅ વેરિયેબલ છે
    private String totalAmount;
    private String status;
    private String date;
    private String productId;
    private int quantity;

    public Order() {
    }

    public Order(String orderId, String userId, String totalAmount, String status, String date, String productId, int quantity) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.date = date;
        this.productId = productId;
        this.quantity = quantity;
    }

    // --- Getters and Setters ---

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    // 🔥 આ બે ફંક્શન ખૂટતા હતા, એટલે એરર આવતી હતી. હવે ઉમેરી દીધા છે.
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTotalAmount() { return String.valueOf(totalAmount); }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
