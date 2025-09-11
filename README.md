# Inventory Management API

A REST API for inventory management built with Spring Boot, featuring product-supplier relationships, stock tracking, and test coverage. Demonstrates Java development practices and API design.

## 🎯 Technical Highlights

This project showcases:

- **Relational Data Modeling**: Many-to-many relationships between Products and Suppliers with proper junction tables
- **Dynamic Querying**: JPA Specifications for flexible search criteria across multiple fields
- **Test Coverage**: 64 integration tests covering main workflows and edge cases
- **Proper Error Handling**: Custom exceptions with meaningful HTTP status codes and error messages
- **API Documentation**: Auto-generated Swagger documentation with request/response examples
- **Layered Architecture**: Clear separation between controllers, services, and data access layers

## 🚀 Core Features

### Product Management

- CRUD operations with validation
- SKU-based unique identification
- Stock level tracking with minimum thresholds
- Soft delete implementation
- Multi-field search (name, SKU, description, price ranges)

### Supplier Management

- Supplier profiles with business validation
- Domestic and international supplier types
- Rating system (1.0-5.0 range validation)
- Business ID uniqueness constraints
- Standard CRUD operations

### Stock Movement Tracking

- Inventory transaction logging (IN/OUT movements)
- Movement reasons (Purchase, Sale, Adjustment, Return)
- Automatic stock calculations
- Insufficient stock validation
- Audit trail with timestamps

### Database Relationships

- **Many-to-Many**: Products ↔ Suppliers using junction table
- **One-to-Many**: Products → Stock Movements
- Foreign key constraints and referential integrity

## 🛠️ Tech Stack & Architecture

### Backend Stack

- **Java 17** - LTS version
- **Spring Boot 3.5.5** - Framework with auto-configuration
- **Spring Data JPA** - ORM with JPA Specifications for dynamic queries
- **PostgreSQL 15** - Relational database
- **H2 Database** - In-memory database for testing

### Development Tools

- **MapStruct** - Compile-time DTO mapping
- **Bean Validation (Jakarta)** - Declarative validation annotations
- **Lombok** - Boilerplate reduction
- **Docker & Docker Compose** - Local development environment

### Documentation & Testing

- **OpenAPI 3 / Swagger** - API documentation with interactive UI
- **JUnit 5** - Testing framework
- **Spring Boot Test** - Integration testing support
- **TestRestTemplate** - HTTP testing

## 📋 Prerequisites

- **Java 17+** (Project uses LTS features)
- **Docker & Docker Compose** (For PostgreSQL database)
- **Maven 3.6+** (Build and dependency management)

## 🏃 Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd inventory-api
   ```

2. **Start PostgreSQL**
   ```bash
   docker-compose up -d
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Verify it's running**
   ```bash
   curl http://localhost:8080/api/v1/products
   ```

5. **Access API Documentation**
    - **Swagger UI**: http://localhost:8080/swagger-ui/index.html
    - **OpenAPI JSON**: http://localhost:8080/api-docs

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Endpoint Reference

#### 🛍️ Product Management

| Method   | Endpoint                         | Description                               |
|----------|----------------------------------|-------------------------------------------|
| `POST`   | `/products`                      | Create product with suppliers             |
| `GET`    | `/products`                      | List products (paginated)                 |
| `GET`    | `/products/{id}`                 | Get product by ID                         |
| `GET`    | `/products/sku/{sku}`            | Get product by SKU                        |
| `PUT`    | `/products/{id}`                 | Update product                            |
| `DELETE` | `/products/{id}`                 | Soft delete product (requires zero stock) |
| `PUT`    | `/products/{id}/suppliers`       | Update product suppliers                  |
| `GET`    | `/products/{id}/stock-movements` | Get product movement history              |

#### 🏢 Supplier Management

| Method | Endpoint                   | Description                       |
|--------|----------------------------|-----------------------------------|
| `POST` | `/suppliers`               | Create supplier                   |
| `GET`  | `/suppliers`               | List suppliers (paginated)        |
| `GET`  | `/suppliers/{id}`          | Get supplier by ID                |
| `PUT`  | `/suppliers/{id}`          | Update supplier (status required) |
| `GET`  | `/suppliers/{id}/products` | Get supplier's products           |

#### 📦 Stock Movement Tracking

| Method | Endpoint           | Description                |
|--------|--------------------|----------------------------|
| `POST` | `/stock-movements` | Create stock movement      |
| `GET`  | `/stock-movements` | List movements (paginated) |

#### 🔍 Search Endpoints

| Endpoint            | Parameters                                                                                                       | Description          |
|---------------------|------------------------------------------------------------------------------------------------------------------|----------------------|
| `/products/search`  | `name`, `category`, `sku`, `description`, `minPrice`, `maxPrice`, `minStock`, `maxStock`, `lowStock`, pagination | Product filtering    |
| `/suppliers/search` | `name`, `supplierType`, `status`, `minRating`, `maxRating`, `maxDeliveryDays`, pagination                        | Supplier filtering   |

### 📄 Sample API Requests

#### Create Supplier

POST /api/v1/suppliers

```json
{
  "name": "TechCorp International",
  "businessId": "TC2024001",
  "status": "ACTIVE",
  "email": "contact@techcorp.com",
  "phone": "+1-555-0123",
  "contactPerson": "John Smith",
  "address": {
    "streetAddress": "123 Tech Street",
    "city": "San Francisco",
    "stateProvince": "CA",
    "postalCode": "94105",
    "country": "USA"
  },
  "paymentTerms": "Net 30 days",
  "averageDeliveryDays": 5,
  "supplierType": "DOMESTIC",
  "rating": 4.5,
  "notes": "Premium supplier for electronic components"
}
```

#### Create Product (with Suppliers)

POST /api/v1/products

```json
{
  "name": "Wireless Gaming Mouse",
  "description": "High-precision wireless gaming mouse with RGB lighting",
  "sku": "WGM-001",
  "price": 79.99,
  "stockQuantity": 50,
  "minStockLevel": 10,
  "category": "electronics",
  "supplierIds": [
    "550e8400-e29b-41d4-a716-446655440000"
  ]
}
```

#### Create Stock Movement

POST /api/v1/stock-movements

```json
{
  "productId": "123e4567-e89b-12d3-a456-426614174000",
  "movementType": "IN",
  "quantity": 25,
  "reason": "PURCHASE",
  "reference": "PO-2024-001",
  "notes": "Weekly inventory replenishment"
}
```

#### Search Examples

```bash
# Product search with filters
GET /api/v1/products/search?category=electronics&minPrice=50&maxPrice=100&lowStock=true&page=0&size=10&sort=price,desc

# Supplier search by type and rating
GET /api/v1/suppliers/search?supplierType=DOMESTIC&minRating=4.0&maxDeliveryDays=7

# Find suppliers by name and status
GET /api/v1/suppliers/search?name=Tech&status=ACTIVE&page=0&size=5
```

## 🧪 Testing Approach

### Test Coverage

- **64 Integration Tests** across 5 test classes
- **400+ Total Tests** covering all layers
- Happy path and edge case scenarios
- Validation error handling with proper HTTP status codes
- Pagination and search boundary testing
- Relationship integrity testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ProductControllerIntegrationTest
```

### Notable Test Scenarios

- Product deletion blocked when stock > 0 (returns 422)
- Supplier updates require status field (400 validation)
- Stock movements prevent insufficient inventory
- Product-Supplier relationship handling
- Search with special characters and edge cases

## 🏗️ Architecture Overview

### Project Structure

```
├── Controller Layer    → REST endpoints, request/response handling
├── Service Layer      → Business logic and validations
├── Repository Layer   → Data access with Spring Data JPA
└── Entity Layer       → JPA entities and relationships
```

### Patterns Used

- **Repository Pattern**: Spring Data JPA repositories
- **DTO Pattern**: Request/Response objects with MapStruct
- **Specification Pattern**: Dynamic queries with JPA Specifications
- **Global Exception Handling**: @ControllerAdvice for centralized error handling
- **Soft Delete**: Mark records as inactive instead of physical deletion
- **Factory Pattern**: Test data builders for consistent test setup

## 🔧 Configuration

### Development

```bash
# PostgreSQL via Docker
docker-compose up -d

# Application properties
spring.profiles.active=dev
```

### Production Environment Variables

- `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE=prod`

## 📁 Project Structure

```
src/
├── main/java/com/inventory/
│   ├── controller/          # REST API endpoints
│   │   ├── ProductController.java
│   │   ├── SupplierController.java
│   │   └── StockMovementController.java
│   ├── dto/                # Data Transfer Objects
│   │   ├── request/        # API request DTOs
│   │   └── response/       # API response DTOs
│   ├── entity/             # JPA entities with relationships
│   │   ├── BaseEntity.java # Audit fields (created/updated timestamps)
│   │   ├── Product.java    # Product entity with supplier relationships
│   │   ├── Supplier.java   # Supplier entity with validations
│   │   └── StockMovement.java # Stock movement audit trail
│   ├── enums/              # Business enumerations
│   ├── exception/          # Custom exceptions and global handler
│   ├── mapper/             # MapStruct interface mappers
│   ├── repository/         # JPA repositories with custom queries
│   ├── service/            # Business logic layer
│   └── specification/      # JPA Specifications for dynamic queries
└── test/java/com/inventory/
    ├── integration/        # Integration tests
    │   └── controller/     # Controller integration tests
    └── fixtures/           # Test data factories
```

## 🚀 Development Journey

### Project Evolution

This project grew incrementally to demonstrate various Spring Boot concepts:

- **Started** with basic Product CRUD operations
- **Added** Supplier management and many-to-many relationships
- **Implemented** Stock movement tracking for audit trails
- **Enhanced** with validation, error handling, and search capabilities
- **Covered** with integration tests for main workflows

### Development Practices

- **Layered Architecture**: Clear separation of concerns
- **Input Validation**: Bean validation with proper error responses
- **Test Coverage**: Integration tests for API endpoints
- **API Documentation**: Swagger for interactive documentation
- **Code Quality**: MapStruct for type-safe mapping, Lombok for cleaner code

## 🏥 Health & Monitoring

```bash
# Application health check
curl http://localhost:8080/actuator/health

# Application metrics
curl http://localhost:8080/actuator/metrics

# Application info
curl http://localhost:8080/actuator/info
```

## 🔗 Additional Resources

- **Live API Documentation**: Available at `/swagger-ui/index.html` when running
- **Database Console**: H2 console available at `/h2-console` (test profile)
- **Actuator Endpoints**: Available at `/actuator` for monitoring
- **OpenAPI Spec**: Raw specification at `/api-docs`

---

## 💼 Portfolio Context

This project demonstrates Java development skills including:

- **JPA Relationships**: Many-to-many and one-to-many relationships
- **Spring Boot Features**: Auto-configuration, validation, exception handling
- **API Design**: RESTful endpoints with HTTP status codes and error responses
- **Testing**: Integration and unit tests covering various scenarios
- **Database Design**: Normalized schema with constraints
- **Documentation**: Swagger/OpenAPI integration

**Tech Stack**: Uses Spring Boot, JPA, PostgreSQL, and related tools common in Java web development.