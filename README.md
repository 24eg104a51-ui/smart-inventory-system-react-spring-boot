# Smart Inventory & Demand Forecaster

A college capstone project featuring Spring Boot backend, Angular frontend, and Weka ML for demand forecasting.

## Architecture

```
[ React Frontend ] ←REST/JSON→ [ Spring Boot Backend ] ←JPA→ [ MySQL Database ]
                                                        ↓
                                              [ Weka ML Engine ]
                                              (Linear Regression)
```

## Project Structure

```
smart-inventory-forecaster/
├── backend/                 # Spring Boot Maven Project
│   ├── src/main/java/com/project/inventory/
│   │   ├── config/          # Security & CORS configuration
│   │   ├── controller/      # REST Endpoints (Product, Stock, Forecast)
│   │   ├── model/           # JPA Entities (Product, Transaction, ForecastResult)
│   │   ├── repository/      # Data Access Layers (Spring Data JPA)
│   │   ├── service/         # Core Business Logic
│   │   └── ml/              # Weka Integration & Forecasting Logic
│   └── pom.xml              # Dependencies (Weka, Spring Web, MySQL Connector)
│
└── frontend/                # Angular Workspace
    ├── src/app/
    │   ├── components/      # Dashboard, ProductList, ForecastView, TransactionHistory
    │   ├── models/          # TypeScript interfaces
    │   ├── services/        # API HTTP Client communication
    │   └── app-routing.module.ts
    └── package.json
```

## Features

- **Product Management**: CRUD operations for inventory products
- **Transaction Tracking**: Record purchases, sales, returns, and adjustments
- **Stock Alerts**: Automatic low-stock detection
- **AI Demand Forecasting**: Weka Linear Regression for 30-day predictions
- **Dashboard**: Real-time overview with stats and charts
- **Responsive UI**: Bootstrap 5 + React 

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- Angular CLI 17+
- MySQL 8.0+

## Backend Setup

```bash
cd backend

# Configure database in src/main/resources/application.properties
# spring.datasource.username=root
# spring.datasource.password=your_password

# Run
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`

## Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm start
```

Frontend runs on `http://localhost:4200`

## API Endpoints

### Products
- `GET /api/products` - List all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/search?q=` - Search products
- `GET /api/products/low-stock` - Get low stock products
- `PATCH /api/products/{id}/stock?change=` - Adjust stock

### Transactions
- `POST /api/transactions` - Record transaction
- `GET /api/transactions/product/{productId}` - Get transactions (paginated)
- `GET /api/transactions/product/{productId}/all` - Get all transactions
- `GET /api/transactions/product/{productId}/period?start=&end=` - Get transactions in period
- `GET /api/transactions/product/{productId}/total-sales?start=&end=` - Get total sales
- `DELETE /api/transactions/{id}` - Delete transaction

### Forecasts
- `POST /api/forecasts/product/{productId}` - Generate single forecast
- `POST /api/forecasts/product/{productId}/multi-day?days=30` - Generate multi-day forecast
- `GET /api/forecasts/product/{productId}/latest` - Get latest forecast
- `GET /api/forecasts/product/{productId}/history` - Get forecast history
- `GET /api/forecasts/product/{productId}/upcoming?fromDate=` - Get upcoming forecasts
- `DELETE /api/forecasts/product/{productId}` - Delete forecasts

## ML Forecasting Details

The system uses **Weka's LinearRegression** classifier with the following features:
- Day of week, day of month, month
- Lag features: 1-day, 7-day, 30-day historical sales
- 5-fold cross-validation for accuracy estimation
- Fallback to average-based prediction when insufficient data

## Development

### Running Tests
```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

### Building for Production
```bash
# Backend
cd backend && mvn clean package

# Frontend
cd frontend && npm run build
```

## License

MIT License - College Capstone Project