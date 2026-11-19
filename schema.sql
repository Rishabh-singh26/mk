-- ===================================================
-- Retail Store Database Setup Script
-- Database: retail_db
-- ===================================================

-- 1. Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS retail_db;

-- 2. Switch to retail_db
USE retail_db;

-- 3. Create the Product table as per specifications
CREATE TABLE IF NOT EXISTS Product (
    product_id INT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL
);

-- 4. Insert initial sample records (at least three records)
INSERT INTO Product (product_id, product_name, category, quantity, price) VALUES
(101, 'Wireless Mouse', 'Electronics', 15, 599.00),
(102, 'Mechanical Keyboard', 'Electronics', 8, 2499.50),
(103, 'USB-C Fast Cable', 'Accessories', 45, 299.00),
(104, 'Gaming Headset', 'Electronics', 12, 1899.00),
(105, 'Notebook Diary', 'Stationery', 0, 150.00)
ON DUPLICATE KEY UPDATE product_name = VALUES(product_name);
