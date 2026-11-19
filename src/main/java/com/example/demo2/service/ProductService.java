package com.example.demo2.service;

import com.example.demo2.model.Product;
import com.example.demo2.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

/**
 * Service Layer for Inventory Management System.
 * Encapsulates business logic for operations (a) through (e).
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * (a) Insert a product record using PreparedStatement.
     * Appropriately handles duplicate product IDs.
     */
    public String insertProduct(Product product) {
        try {
            boolean inserted = productRepository.insertProduct(product);
            if (inserted) {
                return "Product with ID " + product.getProductId() + " inserted successfully.";
            } else {
                return "Failed to insert product with ID " + product.getProductId() + ".";
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            return "Error: Product ID " + product.getProductId() + " already exists. Duplicate IDs are not allowed.";
        } catch (SQLException e) {
            return "Database Error while inserting product: " + e.getMessage();
        }
    }

    /**
     * (a) Helper to insert at least three sample records.
     */
    public String seedSampleProducts() {
        Product[] samples = {
            new Product(101, "Wireless Mouse", "Electronics", 15, 599.00),
            new Product(102, "Mechanical Keyboard", "Electronics", 8, 2499.50),
            new Product(103, "USB-C Fast Cable", "Accessories", 45, 299.00),
            new Product(104, "Gaming Headset", "Electronics", 12, 1899.00),
            new Product(105, "Notebook Diary", "Stationery", 0, 150.00)
        };

        int count = 0;
        for (Product p : samples) {
            try {
                if (productRepository.insertProduct(p)) {
                    count++;
                }
            } catch (SQLIntegrityConstraintViolationException ignored) {
                // Duplicate skipped
            } catch (SQLException e) {
                return "Database error during seed: " + e.getMessage();
            }
        }
        return "Seeding completed: " + count + " new sample product(s) added.";
    }

    /**
     * (b) Conditional Retrieval and Sorting.
     * Retrieves products belonging to specified category with quantity < 20,
     * sorted by price in descending order.
     */
    public List<Product> getLowStockProductsByCategory(String category) throws SQLException {
        return productRepository.findByCategoryAndQuantityLessThan(category, 20);
    }

    /**
     * (c) Update Product Stock.
     * Adds quantity to existing stock; handles case where product does not exist.
     */
    public String updateStock(int productId, int quantityToAdd) throws SQLException {
        int rowsAffected = productRepository.updateStock(productId, quantityToAdd);
        if (rowsAffected > 0) {
            Optional<Product> updated = productRepository.findById(productId);
            return "Stock updated successfully for Product ID " + productId +
                    ". Current Stock: " + updated.map(Product::getQuantity).orElse(0);
        } else {
            return "Error: Product with ID " + productId + " does not exist. Stock could not be updated.";
        }
    }

    /**
     * (d) Search and Modify Product Details.
     * Updates price only if product exists and new price > 0.
     * Returns the updated product or an error message.
     */
    public String searchAndModifyPrice(int productId, double newPrice) throws SQLException {
        if (newPrice <= 0) {
            return "Validation Error: New price must be greater than zero (> 0).";
        }

        Optional<Product> existingProduct = productRepository.findById(productId);
        if (existingProduct.isEmpty()) {
            return "Error: Product with ID " + productId + " does not exist. Price cannot be updated.";
        }

        productRepository.updatePrice(productId, newPrice);
        Optional<Product> updated = productRepository.findById(productId);
        return "Price updated successfully! Updated Product: " + updated.orElse(null);
    }

    /**
     * (e) Part 1: Display Total Inventory Value using SUM(quantity * price).
     */
    public double getTotalInventoryValue() throws SQLException {
        return productRepository.calculateTotalInventoryValue();
    }

    /**
     * (e) Part 2: Conditional Deletion.
     * Deletes product only if quantity is zero.
     * Displays appropriate message if product cannot be deleted.
     */
    public String deleteProductIfZeroStock(int productId) throws SQLException {
        Optional<Product> existing = productRepository.findById(productId);
        if (existing.isEmpty()) {
            return "Error: Product with ID " + productId + " does not exist. Deletion aborted.";
        }

        Product product = existing.get();
        if (product.getQuantity() == 0) {
            productRepository.deleteById(productId);
            return "Success: Product '" + product.getProductName() + "' (ID: " + productId + ") has 0 stock and was deleted successfully.";
        } else {
            return "Cannot Delete: Product '" + product.getProductName() + "' (ID: " + productId +
                   ") has a quantity of " + product.getQuantity() + " (> 0). Deletion is permitted only when quantity is 0.";
        }
    }

    /**
     * Retrieve a product by ID.
     */
    public Optional<Product> getProductById(int productId) throws SQLException {
        return productRepository.findById(productId);
    }

    /**
     * Retrieve all products.
     */
    public List<Product> getAllProducts() throws SQLException {
        return productRepository.findAll();
    }
}
