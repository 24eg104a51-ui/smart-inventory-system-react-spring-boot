-- MySQL Schema for Smart Inventory & Demand Forecaster
-- Creates all tables if they do not exist.
-- Run manually: mysql -u root -p < database/schema.sql
-- (Spring Boot also auto-executes this on startup via src/main/resources/schema.sql)

CREATE DATABASE IF NOT EXISTS Inventory;
USE Inventory;

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    current_stock INT NOT NULL,
    min_stock_threshold INT DEFAULT 10,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    notes VARCHAR(255),
    CONSTRAINT fk_transaction_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Forecast results table
CREATE TABLE IF NOT EXISTS forecast_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    forecast_date DATE NOT NULL,
    predicted_demand INT NOT NULL,
    confidence_lower INT,
    confidence_upper INT,
    model_used VARCHAR(50),
    accuracy_score DECIMAL(5,4),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_forecast_product FOREIGN KEY (product_id) REFERENCES products(id)
);
