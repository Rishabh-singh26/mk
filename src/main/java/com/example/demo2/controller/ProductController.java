package com.example.demo2.controller;

import com.example.demo2.model.Product;
import com.example.demo2.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing endpoints for the Inventory Management System.
 * Mapped to operations (a) through (e).
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * (a) Insert Product Record
     * Handles duplicate product IDs appropriately.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> insertProduct(@RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        String message = productService.insertProduct(product);
        response.put("message", message);

        if (message.startsWith("Error:") || message.startsWith("Database Error")) {
            response.put("status", "FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("status", "SUCCESS");
        response.put("product", product);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * (a) Seed at least 3 initial sample products
     */
    @PostMapping("/seed")
    public ResponseEntity<Map<String, String>> seedSampleProducts() {
        String result = productService.seedSampleProducts();
        Map<String, String> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    /**
     * (b) Conditional Retrieval and Sorting
     * Displays all products in category whose quantity < 20, sorted by price DESC.
     */
    @GetMapping("/category/{category}/low-stock")
    public ResponseEntity<List<Product>> getLowStockByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getLowStockProductsByCategory(category);
            return ResponseEntity.ok(products);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * (c) Update Product Stock
     * Adds quantity to existing stock; handles non-existent product IDs.
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Map<String, String>> updateStock(
            @PathVariable int id,
            @RequestParam int addQuantity) {

        Map<String, String> response = new HashMap<>();
        try {
            String message = productService.updateStock(id, addQuantity);
            response.put("message", message);

            if (message.startsWith("Error:")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * (d) Search and Modify Product Details
     * Updates price only if product exists and new price > 0.
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<Map<String, String>> updatePrice(
            @PathVariable int id,
            @RequestParam double newPrice) {

        Map<String, String> response = new HashMap<>();
        try {
            String message = productService.searchAndModifyPrice(id, newPrice);
            response.put("message", message);

            if (message.startsWith("Validation Error:")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else if (message.startsWith("Error:")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * (e) Part 1: Display Total Inventory Value
     * Uses SUM(quantity * price).
     */
    @GetMapping("/inventory-value")
    public ResponseEntity<Map<String, Object>> getTotalInventoryValue() {
        Map<String, Object> response = new HashMap<>();
        try {
            double totalValue = productService.getTotalInventoryValue();
            response.put("totalInventoryValue", totalValue);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * (e) Part 2: Conditional Deletion
     * Deletes record only if quantity is zero.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProductIfZeroStock(@PathVariable int id) {
        Map<String, String> response = new HashMap<>();
        try {
            String message = productService.deleteProductIfZeroStock(id);
            response.put("message", message);

            if (message.startsWith("Cannot Delete:")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else if (message.startsWith("Error:")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Utility: Get all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Utility: Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id) {
        try {
            return productService.getProductById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
