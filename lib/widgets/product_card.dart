import 'package:flutter/material.dart';
import '../models/product_model.dart';

class ProductCard extends StatelessWidget {
  final ProductModel product;
  const ProductCard({required this.product});

  String calculateDiscount(String? original, String? selling) {
    if (original == null || selling == null) return "0";
    double oldPrice = double.tryParse(original) ?? 0.0;
    double newPrice = double.tryParse(selling) ?? 0.0;
    
    if (oldPrice > 0) {
      double percentage = ((oldPrice - newPrice) / oldPrice) * 100;
      return percentage.toStringAsFixed(0);
    }
    return "0";
  }

  @override
  Widget build(BuildContext context) {
    String discount = calculateDiscount(product.originalPrice, product.price);

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      elevation: 3,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // BIG DISCOUNT CONTAINER (No Image)
          Container(
            height: 150,
            width: double.infinity,
            decoration: BoxDecoration(
              color: Color(0xFFF9F0F2), // Light theme color
              borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
            ),
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    "$discount%",
                    style: TextStyle(
                      fontSize: 42, 
                      fontWeight: FontWeight.bold, 
                      color: Color(0xFF880E4F)
                    ),
                  ),
                  Text(
                    "OFF",
                    style: TextStyle(
                      fontSize: 18, 
                      fontWeight: FontWeight.bold, 
                      color: Color(0xFF880E4F)
                    ),
                  ),
                ],
              ),
            ),
          ),
          
          Padding(
            padding: const EdgeInsets.all(12.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  product.name ?? "",
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
                ),
                // Show ONLY shortDescription here
                Text(
                  product.shortDescription ?? "",
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 13, color: Colors.grey[600]),
                ),
                SizedBox(height: 8),
                Text(
                  "₹ ${product.price}",
                  style: TextStyle(
                    fontSize: 16, 
                    fontWeight: FontWeight.bold, 
                    color: Color(0xFF1A56C4)
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
