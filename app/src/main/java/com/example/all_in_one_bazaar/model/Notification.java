package com.example.all_in_one_bazaar.model; // 🛠️ FIX: પેકેજ નામ સ્મોલ અક્ષરોમાં કર્યું

public class Notification {
    String id, title, message, type, timestamp;

    public Notification() { }

    public Notification(String id, String title, String message, String type, String timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; } // type: "offer", "order_placed", "order_delivered"

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}