class ProductModel {
  String? id;
  String? name;
  String? shortDescription;
  String? longDescription;
  String? price;
  String? originalPrice;
  String? image;
  String? category;
  String? rating;
  String? stockStatus;
  String? quantity;

  ProductModel({
    this.id,
    this.name,
    this.shortDescription,
    this.longDescription,
    this.price,
    this.originalPrice,
    this.image,
    this.category,
    this.rating,
    this.stockStatus,
    this.quantity,
  });

  factory ProductModel.fromMap(Map<String, dynamic> data, String docId) {
    return ProductModel(
      id: docId,
      name: data['name'],
      shortDescription: data['shortDescription'] ?? "",
      longDescription: data['longDescription'] ?? "",
      price: data['price'],
      originalPrice: data['originalPrice'],
      image: data['image'],
      category: data['category'],
      rating: data['rating'],
      stockStatus: data['stockStatus'],
      quantity: data['quantity'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      "name": name,
      "shortDescription": shortDescription,
      "longDescription": longDescription,
      "price": price,
      "originalPrice": originalPrice,
      "image": image,
      "category": category,
      "rating": rating,
      "stockStatus": stockStatus,
      "quantity": quantity,
    };
  }
}
