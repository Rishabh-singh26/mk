package com.example.demo2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Inventory Management System
 * Menu-driven Java JDBC Application using MySQL
 *
 * Database: retail_db
 * Table: Product (product_id, product_name, category, quantity, price)
 */
public class InventoryManagementSystem {

    // Database Configuration - Update credentials according to your MySQL environment
    private static final String DB_URL = "jdbc:mysql://localhost:3306/retail_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root"; // Change to your MySQL password

    private static Scanner scanner = new Scanner(System.in);

    /**
     * Helper method to establish and return a database connection.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void main(String[] args) {
        // Test connection at startup
        try (Connection conn = getConnection()) {
            System.out.println("=================================================");
            System.out.println(" Connected successfully to MySQL Database: retail_db");
            System.out.println("=================================================");
        } catch (SQLException e) {
            System.err.println("Warning: Could not connect to database on startup.");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Ensure MySQL server is running and retail_db exists.\n");
        }

        while (true) {
            System.out.println("\n=================================================");
            System.out.println("        RETAIL INVENTORY MANAGEMENT SYSTEM       ");
            System.out.println("=================================================");
            System.out.println("1. (a) Insert Product Record (Single / Batch)");
            System.out.println("2. (a) Seed Initial 3 Sample Products");
            System.out.println("3. (b) Conditional Retrieval & Sorting (Category & Qty < 20)");
            System.out.println("4. (c) Update Product Stock (Add Quantity)");
            System.out.println("5. (d) Search & Modify Product Price (Price > 0)");
            System.out.println("6. (e) Display Total Inventory Value & Delete Zero-Stock Product");
            System.out.println("7. Display All Products");
            System.out.println("8. Exit");
            System.out.println("=================================================");
            System.out.print("Enter your choice (1-8): ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 1 and 8.");
                continue;
            }

            switch (choice) {
                case 1:
                    insertProductInteractive();
                    break;
                case 2:
                    insertSampleProducts();
                    break;
                case 3:
                    retrieveAndSortByCategory();
                    break;
                case 4:
                    updateProductStock();
                    break;
                case 5:
                    searchAndModifyPrice();
                    break;
                case 6:
                    inventoryValueAndConditionalDelete();
                    break;
                case 7:
                    displayAllProducts();
                    break;
                case 8:
                    System.out.println("Exiting Inventory Management System. Goodbye!");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice! Please select a valid option (1-8).");
            }
        }
    }

    // =========================================================================
    // (a) Insert Product Records — 2 Marks
    // Establish connection and insert records using PreparedStatement.
    // Handle duplicate product IDs appropriately.
    // =========================================================================
    public static void insertProductInteractive() {
        System.out.println("\n--- (a) Insert New Product Record ---");
        try {
            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Product Name: ");
            String productName = scanner.nextLine().trim();

            System.out.print("Enter Category: ");
            String category = scanner.nextLine().trim();

            System.out.print("Enter Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Price: ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            boolean success = insertProduct(productId, productName, category, quantity, price);
            if (success) {
                System.out.println(" SUCCESS: Product inserted successfully!");
            }
        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid number format for ID, Quantity, or Price.");
        }
    }

    /**
     * Inserts at least three sample product records using PreparedStatement
     * and handles duplicate product IDs gracefully.
     */
    public static void insertSampleProducts() {
        System.out.println("\n--- (a) Inserting At Least 3 Sample Products ---");
        Object[][] sampleData = {
            {101, "Wireless Mouse", "Electronics", 15, 599.00},
            {102, "Mechanical Keyboard", "Electronics", 8, 2499.50},
            {103, "USB-C Fast Cable", "Accessories", 45, 299.00},
            {104, "Gaming Headset", "Electronics", 12, 1899.00},
            {105, "Notebook Diary", "Stationery", 0, 150.00}
        };

        int insertedCount = 0;
        for (Object[] item : sampleData) {
            int id = (int) item[0];
            String name = (String) item[1];
            String cat = (String) item[2];
            int qty = (int) item[3];
            double price = (double) item[4];

            if (insertProduct(id, name, cat, qty, price)) {
                insertedCount++;
            }
        }
        System.out.println(" Sample insertion completed. " + insertedCount + " new record(s) inserted.");
    }

    /**
     * Executes the parameterized INSERT statement and catches duplicate key exceptions.
     */
    public static boolean insertProduct(int productId, String name, String category, int quantity, double price) {
        String sql = "INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, price);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Handles duplicate product_id primary key violation
            System.out.println(" [Duplicate ID Error]: Product ID " + productId + " already exists in the database. Insertion skipped.");
            return false;
        } catch (SQLException e) {
            System.out.println(" Database Error during insert: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // (b) Conditional Retrieval and Sorting — 2 Marks
    // Accept category and display all products where quantity < 20.
    // Sort results by price in descending order.
    // =========================================================================
    public static void retrieveAndSortByCategory() {
        System.out.println("\n--- (b) Conditional Retrieval & Sorting (Quantity < 20, Ordered by Price DESC) ---");
        System.out.print("Enter Category to search: ");
        String searchCategory = scanner.nextLine().trim();

        String sql = "SELECT product_id, product_name, category, quantity, price " +
                     "FROM Product " +
                     "WHERE category = ? AND quantity < 20 " +
                     "ORDER BY price DESC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchCategory);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                System.out.println("\n---------------------------------------------------------------------------------------------------");
                System.out.printf("%-12s %-30s %-20s %-12s %-12s%n", "Product ID", "Product Name", "Category", "Quantity", "Price (INR)");
                System.out.println("---------------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    found = true;
                    int id = rs.getInt("product_id");
                    String name = rs.getString("product_name");
                    String category = rs.getString("category");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    System.out.printf("%-12d %-30s %-20s %-12d %-12.2f%n", id, name, category, quantity, price);
                }

                if (!found) {
                    System.out.println("No products found in category '" + searchCategory + "' with quantity < 20.");
                }
                System.out.println("---------------------------------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.out.println(" Database Error during retrieval: " + e.getMessage());
        }
    }

    // =========================================================================
    // (c) Update Product Stock — 2 Marks
    // Accept product ID and quantity to add to existing stock.
    // Update quantity and display appropriate message if product does not exist.
    // =========================================================================
    public static void updateProductStock() {
        System.out.println("\n--- (c) Update Product Stock ---");
        try {
            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Quantity to Add: ");
            int quantityToAdd = Integer.parseInt(scanner.nextLine().trim());

            if (quantityToAdd < 0) {
                System.out.println(" Note: You entered a negative quantity. This will decrease stock.");
            }

            // Update quantity: existing quantity + quantityToAdd
            String updateSql = "UPDATE Product SET quantity = quantity + ? WHERE product_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                pstmt.setInt(1, quantityToAdd);
                pstmt.setInt(2, productId);

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println(" SUCCESS: Stock updated successfully for Product ID " + productId + "!");

                    // Display current updated stock
                    displayProductById(productId);
                } else {
                    System.out.println(" ERROR: Product with ID " + productId + " does not exist. Stock could not be updated.");
                }

            } catch (SQLException e) {
                System.out.println(" Database Error during stock update: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid number entered.");
        }
    }

    // =========================================================================
    // (d) Search and Modify Product Details — 2 Marks
    // Accept product ID and new price.
    // Retrieve product details and update price ONLY if product exists and new price > 0.
    // Display updated product details.
    // =========================================================================
    public static void searchAndModifyPrice() {
        System.out.println("\n--- (d) Search and Modify Product Price ---");
        try {
            System.out.print("Enter Product ID: ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter New Price: ");
            double newPrice = Double.parseDouble(scanner.nextLine().trim());

            // Validate that the new price is greater than zero
            if (newPrice <= 0) {
                System.out.println(" VALIDATION ERROR: New price must be greater than zero (> 0). Update aborted.");
                return;
            }

            // Step 1: Check if product exists and fetch current details
            String selectSql = "SELECT product_id, product_name, category, quantity, price FROM Product WHERE product_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

                selectStmt.setInt(1, productId);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(" ERROR: Product with ID " + productId + " does not exist. Price could not be updated.");
                        return;
                    }

                    System.out.println("\nExisting Product Details:");
                    System.out.println("  ID       : " + rs.getInt("product_id"));
                    System.out.println("  Name     : " + rs.getString("product_name"));
                    System.out.println("  Category : " + rs.getString("category"));
                    System.out.println("  Quantity : " + rs.getInt("quantity"));
                    System.out.println("  Old Price: " + rs.getDouble("price"));
                }

                // Step 2: Update price since product exists and newPrice > 0
                String updateSql = "UPDATE Product SET price = ? WHERE product_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, newPrice);
                    updateStmt.setInt(2, productId);
                    updateStmt.executeUpdate();

                    System.out.println("\n SUCCESS: Price updated successfully to " + String.format("%.2f", newPrice));
                    System.out.println("\nUpdated Product Details:");
                    displayProductById(productId);
                }

            } catch (SQLException e) {
                System.out.println(" Database Error during price modification: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid number entered.");
        }
    }

    // =========================================================================
    // (e) Aggregate Query and Conditional Deletion — 2 Marks
    // Display total inventory value using SUM(quantity * price).
    // Accept product ID and delete record ONLY if quantity is zero.
    // Display appropriate message if product cannot be deleted.
    // =========================================================================
    public static void inventoryValueAndConditionalDelete() {
        System.out.println("\n--- (e) Aggregate Inventory Value & Conditional Deletion ---");

        // Part 1: Aggregate Query: SUM(quantity * price)
        String aggregateSql = "SELECT SUM(quantity * price) AS total_inventory_value FROM Product";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(aggregateSql)) {

            double totalValue = 0.0;
            if (rs.next()) {
                totalValue = rs.getDouble("total_inventory_value");
            }
            System.out.println("---------------------------------------------------------");
            System.out.printf(" TOTAL INVENTORY VALUE: %.2f%n", totalValue);
            System.out.println("---------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println(" Database Error calculating inventory value: " + e.getMessage());
            return;
        }

        // Part 2: Conditional Deletion based on product ID and quantity == 0
        try {
            System.out.print("\nEnter Product ID to delete (Record deleted only if quantity == 0): ");
            int productId = Integer.parseInt(scanner.nextLine().trim());

            String checkSql = "SELECT product_name, quantity FROM Product WHERE product_id = ?";

            try (Connection conn = getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

                checkStmt.setInt(1, productId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println(" ERROR: Product with ID " + productId + " does not exist. Cannot delete.");
                        return;
                    }

                    String name = rs.getString("product_name");
                    int quantity = rs.getInt("quantity");

                    // Check if quantity is zero
                    if (quantity == 0) {
                        String deleteSql = "DELETE FROM Product WHERE product_id = ?";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                            deleteStmt.setInt(1, productId);
                            deleteStmt.executeUpdate();
                            System.out.println(" SUCCESS: Product '" + name + "' (ID: " + productId + ") has quantity = 0 and was deleted successfully.");
                        }
                    } else {
                        System.out.println(" CANNOT DELETE: Product '" + name + "' (ID: " + productId + ") has quantity = " + quantity + " (> 0).");
                        System.out.println(" Condition failed: Records can only be deleted when quantity is strictly 0.");
                    }
                }

            } catch (SQLException e) {
                System.out.println(" Database Error during conditional deletion: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            System.out.println(" Error: Invalid Product ID entered.");
        }
    }

    // =========================================================================
    // Utility Method: Display all products
    // =========================================================================
    public static void displayAllProducts() {
        System.out.println("\n--- All Products in Inventory ---");
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product ORDER BY product_id ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("---------------------------------------------------------------------------------------------------");
            System.out.printf("%-12s %-30s %-20s %-12s %-12s%n", "Product ID", "Product Name", "Category", "Quantity", "Price");
            System.out.println("---------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("product_id");
                String name = rs.getString("product_name");
                String category = rs.getString("category");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                System.out.printf("%-12d %-30s %-20s %-12d %-12.2f%n", id, name, category, quantity, price);
            }

            if (!found) {
                System.out.println("The Product table is currently empty.");
            }
            System.out.println("---------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println(" Database Error displaying products: " + e.getMessage());
        }
    }

    // =========================================================================
    // Utility Method: Display single product details by ID
    // =========================================================================
    private static void displayProductById(int productId) {
        String sql = "SELECT product_id, product_name, category, quantity, price FROM Product WHERE product_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("  ID       : " + rs.getInt("product_id"));
                    System.out.println("  Name     : " + rs.getString("product_name"));
                    System.out.println("  Category : " + rs.getString("category"));
                    System.out.println("  Quantity : " + rs.getInt("quantity"));
                    System.out.println("  Price    : " + rs.getDouble("price"));
                }
            }
        } catch (SQLException e) {
            System.out.println(" Error fetching product details: " + e.getMessage());
        }
    }
}
