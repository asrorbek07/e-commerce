# E-Commerce REST API

A Spring Boot e-commerce REST API with JWT authentication, role-based authorization, order processing, and analytics features.

## Features

- **Authentication**: JWT-based user registration and login
- **Product Management**: CRUD operations with search and categorization
- **Order Processing**: Place orders with inventory management  
- **Role-Based Access**: Admin and User roles with different permissions
- **Analytics**: Sales reports and inventory analytics for admins
- **API Documentation**: OpenAPI/Swagger UI integration

## Tech Stack

- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: PostgreSQL with Liquibase migrations
- **Security**: JWT tokens, BCrypt password hashing
- **Testing**: JUnit 5, Testcontainers
- **Documentation**: SpringDoc OpenAPI
- **Build Tool**: Gradle

## Prerequisites

- Java 17+
- Docker & Docker Compose

## Quick Start

1. **Start with Docker Compose:**
```bash
docker-compose up --build
```

2. **Access the application:**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - Health Check: http://localhost:8080/actuator/health

3. **For local development:**
```bash
# Start database only
docker-compose up postgres

# Run application
./gradlew bootRun
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Products
- `GET /api/products` - List products (with pagination)
- `GET /api/products/search` - Search products by keyword
- `GET /api/products/{id}` - Get product details
- `GET /api/products/categories` - Get product categories
- `POST /api/products` - Create product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)
- `DELETE /api/products/{id}` - Delete product (Admin)

### Orders
- `POST /api/orders` - Place order
- `GET /api/orders` - Get user's orders
- `GET /api/orders/all` - Get all orders (Admin)
- `GET /api/orders/{id}` - Get order details
- `PUT /api/orders/{id}/status` - Update order status (Admin)
- `PUT /api/orders/{id}/cancel` - Cancel order

### Analytics (Admin Only)
- `GET /api/admin/analytics/top-products` - Top selling products
- `GET /api/admin/analytics/low-stock` - Low stock products
- `GET /api/admin/analytics/revenue-report` - Revenue analytics
- `GET /api/admin/analytics/dashboard` - Dashboard data

## Default Users

**Admin:**
- Username: `admin`
- Password: `admin123`

**User:**
- Username: `user` 
- Password: `user123`

## Database Schema

- **users** - User accounts and authentication
- **products** - Product catalog with stock tracking
- **orders** - Customer orders
- **order_items** - Items within orders

## Configuration

### Profiles
- `local` - Development with PostgreSQL on port 5433
- `docker` - Container deployment  
- `test` - Testing with H2 database

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` | Active profile |
| `JWT_SECRET` | Auto-generated | JWT signing key |

## Testing

```bash
./gradlew test
```

## Project Structure

```
src/main/java/com/example/ecommerce/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Request/Response DTOs
├── exception/      # Exception handling
├── model/          # JPA entities
├── repository/     # Data repositories
├── security/       # Security configuration
└── service/        # Business logic
```

## Docker Configuration

The application uses a multi-container setup:
- **app**: Spring Boot application
- **postgres**: PostgreSQL database

Database runs on port 5432 with health checks and persistent storage.

## Development

1. Database migrations are managed by Liquibase
2. The application uses optimistic locking for products
3. Orders are processed with inventory validation
4. All endpoints are documented with OpenAPI annotations