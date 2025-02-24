// code review - דניאל כהן שדה 208565820 עשיתי על לידור מליכי 318515947
// עשיתי על הקבצים הבאים:
// ProductService
// ProductController
// Product
 
// מאחר והקוד מודולרי קיבצתי ת הקבצים הרלוונטים לאותו נושא ועליהם עשיתי את בקרת הקוד:

package com.Supplify.Supplify.services;
import com.Supplify.Supplify.entities.Product;
import com.Supplify.Supplify.repositories.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepo productRepository;

    @Autowired
    public ProductService(ProductRepo productRepository) {
        this.productRepository = productRepository;
    }

    // Retrieve all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Retrieve a product by ID
    public Optional<Product> getProductById(int productId) {
        return productRepository.findById(productId);
    }

    // Add a new product
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    // Update an existing product
    public Product updateProduct(int productId, Product updatedProduct) {
        return productRepository.findById(productId)
                .map(product -> {
                    product.setSupplierId(updatedProduct.getSupplierId());
                    product.setProductName(updatedProduct.getProductName());
                    product.setDescription(updatedProduct.getDescription());
                    product.setBasePrice(updatedProduct.getBasePrice());
                    product.setStockQuantity(updatedProduct.getStockQuantity());
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new IllegalArgumentException("Product with ID " + productId + " not found"));
    }

    // Delete a product by ID
    public void deleteProduct(int productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
        productRepository.deleteById(productId);
    }
    // Find all suppliers that have a specific product
  //  public List<Integer> findSuppliersByProductName(String productName) {
       // return productsRepository.findSuppliersByProductName(productName);
   // }
}

package com.Supplify.Supplify.controllers;

import com.Supplify.Supplify.services.ProductService;
import com.Supplify.Supplify.entities.Product;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    // Get all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Get product by ID
    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable int productId) {
        return productService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a new product
    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.addProduct(product);
            return ResponseEntity.status(201).body(createdProduct);
        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    // Update a product
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable int productId,
            @RequestBody Product updatedProduct) {
        try {
            Product product = productService.updateProduct(productId, updatedProduct);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            logger.warn("Product not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error updating product");
        }
    }

    // Delete a product
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable int productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Product not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting product: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error deleting product");
        }
    }

    // Find all suppliers that provide a specific product
    /*
    @GetMapping("/suppliers/{productName}")
    public ResponseEntity<List<Integer>> findSuppliersByProductName(@PathVariable String productName) {
        try {
            List<Integer> suppliers = productsService.findSuppliersByProductName(productName);
            if (suppliers.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(suppliers);
        } catch (Exception e) {
            logger.error("Error finding suppliers for product '{}': {}", productName, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

     */
}

package com.Supplify.Supplify.entities;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "supplier_id", nullable = false)
    private int supplierId;

    @Column(name = "barcode", nullable = false, length = 50)
    private String barcode;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "cost", nullable = false)
    private int basePrice;

    @Column(name = "selling_price", nullable = false)
    private int selling_price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;
}



// 1. פונקציית addProduct בקובץ ProductService
// תובנות לשיפור הקוד :

// לוודא שאין מוצר אחר עם אותו מזהה ספק ואותו שם מוצר לפני ההוספה. כרגע אין ולידציה שמונעת כפילויות כאלה.
// שימוש בהגדרת חריגות מותאמת אישית (למשל ProductExistsException) עשוי לשפר את קריאות הקוד ואת הטיפול במצבים יוצאי דופן.
// הוספת בדיקות לוגיות לגבי שדות חשובים כמו barcode, productName, ו-basePrice – האם הם חוקיים ומלאים.

// 2. פונקציית updateProduct בקובץ ProductService
// תובנות לשיפור הקוד :

// בעת עדכון המוצר, כדאי לוודא ששדות קריטיים אינם ריקים או שאינם חוקיים, למשל:
// לבדוק את תקינות מזהה הספק (supplierId).
// לבדוק אם המחיר הבסיסי (basePrice) אינו שלילי.
// יש לכלול בדיקות מתקדמות יותר לוודא שהנתונים שהוזנו תקינים (למשל, לא ניתן להקטין את כמות המלאי לערך שלילי).
// שמירה על קונסיסטנטיות בבסיס הנתונים – שימוש בטרנזקציה יכול להועיל אם קיימים תלותים בין נתונים.

// 3. פונקציית deleteProduct בקובץ ProductController
// תובנות לשיפור הקוד :

// כרגע, אם מזהה המוצר אינו קיים, מופיעה שגיאה ברמת הבקר. אפשר לשקול לשפר את חוויית המשתמש על ידי הודעה מותאמת על מחיקה.
// ניתן להוסיף לוג נוסף שיציין אם המוצר נמחק בהצלחה או לא.
// לוודא שבקרת הרשאות קיימת לפני מחיקת מוצר – האם רק משתמשים מסוימים יכולים למחוק?