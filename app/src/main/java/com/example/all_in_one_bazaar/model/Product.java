package com.example.all_in_one_bazaar.model;

import com.google.firebase.database.PropertyName;

public class Product {
    private String id;
    private String name;
    private String price;         // Selling Price
    private String originalPrice;
    private String description;
    private String shortDescription; // Added
    private String longDescription;  // Added
    private String image;
    private String category;
    private String rating; 
    private String stockStatus;
    private String quantity;      // 🔥 એરર રોકવા માટે આને પણ String કરી દીધું છે

    // Default Constructor (Firebase માટે જરૂરી છે)
    public Product() { }

    // Full Constructor
    public Product(String id, String name, String price, String originalPrice, String description, String shortDescription, String longDescription, String image, String category, String rating, String stockStatus, String quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.description = description;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.image = image;
        this.category = category;
        this.rating = rating;
        this.stockStatus = stockStatus;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }

    // 🔥 Firebase માં ફિલ્ડનું નામ 'image' છે પણ અહીં 'imageUrl' વાપરવા માટે
    @PropertyName("image")
    public String getImageUrl() { return image; }

    @PropertyName("image")
    public void setImageUrl(String image) { this.image = image; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}