package com.example.demo2.repository;

import com.example.demo2.model.Product;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) / Repository for Product entity.
 * Uses native Java JDBC PreparedStatement and DataSource.
 */
@Repository
public class ProductRepository {

    private final DataSource dataSource;

    public ProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * (a) Insert Product Record using PreparedStatement.
     * Throws SQLIntegrityConstraintViolationException on duplicate Primary Key (product_id).
     */
    public boolean insertProduct(Product product) throws SQLIntegrityConstraintViolationException, SQLException {
        String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getProductId());
            pstmt.setString(2, product.getProductName());
            pstmt.setString(3, product.getCategory());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setDouble(5, product.getPrice());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * (b) Conditional Retrieval and Sorting.
     * Retrieves products by category where quantity < 20, sorted by price in descending order.
     */
    public List<Product> findByCategoryAndQuantityLessThan(String category, int maxQuantity) throws SQLException {
        String sql = "SELECT product_id, product_name, category, quantity, price " +
                     "FROM Product " +
                     "WHERE category = ? AND quantity < ? " +
                     "ORDER BY price DESC";

        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            pstmt.setInt(2, maxQuantity);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * (c) Update Product Stock.
     * Adds the specified quantity to the existing stock for the given product ID.
     */
    public int updateStock(int productId, int quantityToAdd) throws SQLException {
        String sql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityToAdd);
            pstmt.setInt(2, productId);

            return pstmt.executeUpdate();
        }
    }

    /**
     * (d) Find product by ID.
     */
    public Optional<Product> findById(int productId) throws SQLException {
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * (d) Update Product Price.
     */
    public int updatePrice(int productId, double newPrice) throws SQLException {
        String sql = "UPDATE Product SET price = ? WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, productId);

            return pstmt.executeUpdate();
        }
    }

    /**
     * (e) Aggregate Query: Total Inventory Value using SUM(quantity * price).
     */
    public double calculateTotalInventoryValue() throws SQLException {
        String sql = "SELECT SUM(quantity * price) AS total_val FROM Product";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total_val");
            }
            return 0.0;
        }
    }

    /**
     * (e) Delete product by ID.
     */
    public int deleteById(int productId) throws SQLException {
        String sql = "DELETE FROM Product WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            return pstmt.executeUpdate();
        }
    }

    /**
     * Utility method: Find all products.
     */
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product ORDER BY product_id ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("price")
        );
    }
}
