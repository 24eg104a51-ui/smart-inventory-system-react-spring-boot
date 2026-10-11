-- Seed data for Smart Inventory & Demand Forecaster
-- Run manually: mysql -u root -p Inventory < database/data.sql
-- (Spring Boot auto-executes this on startup via src/main/resources/data.sql)
-- Uses INSERT IGNORE so re-running won't cause duplicate key errors.

USE Inventory;

-- Seed products
INSERT IGNORE INTO products (id, name, description, price, current_stock, min_stock_threshold, created_at, updated_at) VALUES
(1, 'Laptop Pro 15', 'High-performance laptop for professionals', 1299.99, 15, 10, NOW(), NOW()),
(2, 'Wireless Mouse', 'Ergonomic wireless mouse', 29.99, 5, 20, NOW(), NOW()),
(3, 'Mechanical Keyboard', 'RGB mechanical keyboard', 89.99, 0, 10, NOW(), NOW()),
(4, 'USB-C Hub', '7-in-1 USB-C hub', 49.99, 8, 15, NOW(), NOW()),
(5, 'Monitor 27inch', '4K IPS monitor', 349.99, 25, 5, NOW(), NOW()),
(6, 'Webcam HD', '1080p webcam with noise-cancelling mic', 59.99, 12, 10, NOW(), NOW()),
(7, 'Desk Lamp LED', 'Adjustable LED desk lamp', 39.99, 3, 8, NOW(), NOW()),
(8, 'Notebook A5', 'Spiral-bound notebook, 200 pages', 4.99, 200, 50, NOW(), NOW());

-- Seed transactions
INSERT IGNORE INTO transactions (id, product_id, transaction_type, quantity, unit_price, total_amount, transaction_date, notes) VALUES
(1, 1, 'PURCHASE', 50, 1299.99, 64999.50, NOW(), 'Initial stock purchase'),
(2, 1, 'SALE', 35, 1299.99, 45499.65, NOW(), 'Customer order #ORD-2024-001'),
(3, 1, 'SALE', 10, 1299.99, 12999.90, NOW(), 'Customer order #ORD-2024-015'),
(4, 2, 'PURCHASE', 100, 29.99, 2999.00, NOW(), 'Initial stock purchase'),
(5, 2, 'SALE', 80, 29.99, 2399.20, NOW(), 'Online order #WEB-042'),
(6, 2, 'SALE', 15, 29.99, 449.85, NOW(), 'Walk-in customer'),
(7, 3, 'PURCHASE', 50, 89.99, 4499.50, NOW(), 'Initial stock purchase'),
(8, 3, 'SALE', 50, 89.99, 4499.50, NOW(), 'Bulk order from TechCorp'),
(9, 4, 'PURCHASE', 30, 49.99, 1499.70, NOW(), 'Initial stock purchase'),
(10, 4, 'SALE', 22, 49.99, 1099.78, NOW(), 'Customer order #ORD-2024-023'),
(11, 5, 'PURCHASE', 20, 349.99, 6999.80, NOW(), 'Initial stock purchase'),
(12, 5, 'SALE', 5, 349.99, 1749.95, NOW(), 'Corporate client order');

-- Seed forecast results
INSERT IGNORE INTO forecast_results (id, product_id, forecast_date, predicted_demand, confidence_lower, confidence_upper, model_used, accuracy_score, created_at) VALUES
(1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), 12, 8, 16, 'LinearRegression', 0.8500, NOW()),
(2, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), 15, 11, 19, 'LinearRegression', 0.8500, NOW()),
(3, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 14, 10, 18, 'LinearRegression', 0.8500, NOW()),
(4, 1, DATE_ADD(NOW(), INTERVAL 4 DAY), 13, 9, 17, 'LinearRegression', 0.8500, NOW()),
(5, 1, DATE_ADD(NOW(), INTERVAL 5 DAY), 16, 12, 20, 'LinearRegression', 0.8500, NOW()),
(6, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 18, 12, 24, 'LinearRegression', 0.7800, NOW()),
(7, 2, DATE_ADD(NOW(), INTERVAL 2 DAY), 16, 10, 22, 'LinearRegression', 0.7800, NOW()),
(8, 2, DATE_ADD(NOW(), INTERVAL 3 DAY), 14, 9, 19, 'LinearRegression', 0.7800, NOW());
