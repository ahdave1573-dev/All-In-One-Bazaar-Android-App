<div align="center">

# 🛒 All In One Bazaar

### *One Platform. Every Product. Endless Possibilities.*

---

![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat-square&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat-square&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black)
![PHP](https://img.shields.io/badge/PHP-777BB4?style=flat-square&logo=php&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=flat-square&logo=bootstrap&logoColor=white)

![Status](https://img.shields.io/badge/Status-Live%20%F0%9F%9F%A2-brightgreen?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)
![Made With Love](https://img.shields.io/badge/Made%20with-%E2%9D%A4%EF%B8%8F-red?style=flat-square)

</div>

---

## 📖 About The Project

**All In One Bazaar** is a complete e-commerce web platform where users can shop from multiple product categories in one place. It features a full shopping experience — from browsing products to placing orders — along with a powerful admin panel to manage everything behind the scenes.

> 💡 **Idea:** Why visit multiple stores? Get everything at *one bazaar!*

---

## 🚀 Features

<table>
<tr>
<td width="50%">

### 👤 Customer Side
- ✅ Register & Login securely
- ✅ Browse products by category
- ✅ Search for any product
- ✅ View detailed product page
- ✅ Add / Remove from Cart
- ✅ Place & track Orders
- ✅ Manage personal Profile
- ✅ Mobile-friendly design

</td>
<td width="50%">

### 🔧 Admin Panel
- ✅ Dashboard with statistics
- ✅ Add / Edit / Delete Products
- ✅ Manage Categories
- ✅ View & process Orders
- ✅ Manage registered Users
- ✅ Upload product images

</td>
</tr>
</table>

---

## 🛠️ Built With

| Layer           | Technology                      |
|-----------------|---------------------------------|
| 🎨 Frontend     | HTML5, CSS3, JavaScript         |
| ⚙️ Backend      | PHP                             |
| 🗄️ Database     | MySQL                           |
| 🎨 Styling      | Bootstrap + Custom CSS          |
| ☁️ Hosting      | InfinityFree                    |
| 🔧 Version Control | Git & GitHub                 |

---

## 📁 Project Structure

```
All-In-One-Bazaar/
│
├── 📄 index.php                 ← Home Page
├── 📄 login.php                 ← User Login
├── 📄 register.php              ← Registration
├── 📄 logout.php                ← Logout
├── 📄 products.php              ← All Products
├── 📄 product-detail.php        ← Product View
├── 📄 search.php                ← Search Results
├── 📄 cart.php                  ← Shopping Cart
├── 📄 checkout.php              ← Checkout
├── 📄 order-success.php         ← Order Done
├── 📄 profile.php               ← User Profile
├── 📄 my-orders.php             ← Order History
│
├── 📂 admin/
│   ├── index.php                ← Dashboard
│   ├── products.php             ← Manage Products
│   ├── add-product.php          ← Add Product
│   ├── edit-product.php         ← Edit Product
│   ├── categories.php           ← Categories
│   ├── orders.php               ← Manage Orders
│   └── users.php                ← Manage Users
│
├── 📂 includes/
│   ├── db.php                   ← DB Connection
│   ├── header.php               ← Common Header
│   ├── footer.php               ← Common Footer
│   └── functions.php            ← Helper Functions
│
├── 📂 assets/
│   ├── css/                     ← Stylesheets
│   ├── js/                      ← Scripts
│   └── images/                  ← Static Images
│
├── 📂 uploads/                  ← Product Images
└── 📄 README.md
```

---

## ⚙️ Local Setup

### Prerequisites
- XAMPP / WAMP / LAMP
- PHP >= 7.4
- MySQL >= 5.7
- Git

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/ahdave1573-dev/All-In-One-Bazaar.git
```

**2. Move to server folder**
```
XAMPP (Windows) → C:/xampp/htdocs/All-In-One-Bazaar
XAMPP (Mac)     → /Applications/XAMPP/htdocs/All-In-One-Bazaar
Linux           → /var/www/html/All-In-One-Bazaar
```

**3. Import Database**
- Open `http://localhost/phpmyadmin`
- Create database: `allinonebazaar`
- Import: `database/allinonebazaar.sql`

**4. Configure DB connection** → `includes/db.php`
```php
<?php
$host     = "localhost";
$username = "root";
$password = "";
$database = "allinonebazaar";
$conn = mysqli_connect($host, $username, $password, $database);
?>
```

**5. Run**
```
http://localhost/All-In-One-Bazaar/
```

---

## 🔐 Admin Access

```
URL      →  http://localhost/All-In-One-Bazaar/admin/
Email    →  admin@allinonebazaar.com
Password →  admin123
```
> ⚠️ Change credentials after first login!

---

## ☁️ Deployment on InfinityFree

1. Upload files via **FileZilla FTP** or **File Manager**
2. Create MySQL DB from InfinityFree Control Panel
3. Import `.sql` file
4. Update `includes/db.php` with new credentials
5. Visit your domain — done! ✅

---

## 🔮 Future Plans

- [ ] 💳 Payment Gateway (Razorpay / UPI)
- [ ] ⭐ Product Reviews & Ratings
- [ ] 📧 Email Notifications
- [ ] ❤️ Wishlist Feature
- [ ] 🎟️ Coupon / Discount System
- [ ] 🌐 Multi-language (Gujarati / Hindi / English)
- [ ] 📱 PWA Support

---

## 🤝 Contributing

```bash
git checkout -b feature/FeatureName
git commit -m "Add: FeatureName"
git push origin feature/FeatureName
# Then open a Pull Request
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

## 👨‍💻 Developer

| | |
|:---:|:---|
| **Name** | Anshul Dave |
| **Email** | [ahdave1573@gmail.com](mailto:ahdave1573@gmail.com) |
| **GitHub** | [@ahdave1573-dev](https://github.com/ahdave1573-dev) |

---

*Made with ❤️ by **Anshul Dave***

</div>
