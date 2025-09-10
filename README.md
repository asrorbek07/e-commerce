# E-Commerce REST API

A modern, production-ready Spring Boot e-commerce REST API featuring JWT authentication, role-based authorization, comprehensive order processing, and business analytics. Built with enterprise-grade patterns and best practices.

## 🚀 Features

### Core Functionality
- **🔐 Authentication & Authorization**: JWT-based security with role-based access control
- **📦 Product Management**: Full CRUD operations with search, categorization, and inventory tracking
- **🛒 Order Processing**: Complete order lifecycle with inventory validation and status management
- **📊 Business Analytics**: Sales reports, inventory analytics, and dashboard metrics for administrators
- **📚 API Documentation**: Interactive OpenAPI/Swagger UI with comprehensive endpoint documentation

### Technical Features
- **🏗️ Clean Architecture**: Layered design with clear separation of concerns
- **🔄 Database Migrations**: Automated schema management with Liquibase
- **🐳 Containerization**: Docker support with multi-stage builds and health checks
- **🧪 Testing**: Comprehensive test suite with Testcontainers for integration testing
- **📈 Monitoring**: Spring Boot Actuator endpoints for health checks and metrics
- **🔒 Security**: BCrypt password hashing and JWT token-based authentication

## 🛠️ Technology Stack

| Category | Technologies |
|----------|-------------|
| **Framework** | Spring Boot 3.2, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL (production), H2 (testing) |
| **Security** | JWT (JJWT), BCrypt password hashing |
| **Documentation** | SpringDoc OpenAPI 3 |
| **Testing** | JUnit 5, Testcontainers, Spring Boot Test |
| **Build & Deploy** | Gradle, Docker, Docker Compose |
| **Utilities** | Lombok, MapStruct, Liquibase |

## 📋 Prerequisites

- **Java 17+** (OpenJDK or Oracle JDK)
- **Docker & Docker Compose** (for containerized deployment)
- **Gradle** (wrapper included)

## 🚀 Quick Start

### Option 1: Local Development
```bash
# Start only the database
docker-compose -f docker-compose.dev.yml up

# Run the application locally
./gradlew bootRun

# Or on Windows
gradlew.bat bootRun

# The application will be available at:
# - API: http://localhost:8080
# - Swagger UI: http://localhost:8080/swagger-ui/index.html
# - Health Check: http://localhost:8080/actuator/health
```

### Option 2: IDE Development (Recommended)
```
1. Start PostgreSQL using `docker-compose.dev.yml`
2. Set active profile to `local` in your IDE
3. Run `ECommerceApplication.java`
```

### Option 3: Docker Compose
```
bash
# Clone the repository
git clone <repository-url>
cd ecommerce

# Start the entire application stack
docker-compose up --build
```


## 📡 API Reference

### Authentication Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register new user account | ❌ |
| `POST` | `/api/auth/login` | Authenticate and get JWT token | ❌ |

### Product Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/products` | List products with pagination | ❌ |
| `GET` | `/api/products/search?keyword={term}` | Search products by keyword | ❌ |
| `GET` | `/api/products/{id}` | Get product details | ❌ |
| `GET` | `/api/products/categories` | Get all product categories | ❌ |
| `POST` | `/api/products` | Create new product | 👑 Admin |
| `PUT` | `/api/products/{id}` | Update existing product | 👑 Admin |
| `DELETE` | `/api/products/{id}` | Delete product | 👑 Admin |

### Order Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/orders` | Place new order | 🔐 User |
| `GET` | `/api/orders` | Get current user's orders | 🔐 User |
| `GET` | `/api/orders/all` | Get all orders (admin view) | 👑 Admin |
| `GET` | `/api/orders/{id}` | Get order details | 🔐 User/Admin |
| `PUT` | `/api/orders/{id}/status` | Update order status | 👑 Admin |
| `PUT` | `/api/orders/{id}/cancel` | Cancel order | 🔐 User |

### Analytics & Reporting (Admin Only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/analytics/top-products` | Top selling products report |
| `GET` | `/api/admin/analytics/low-stock` | Low inventory alert |
| `GET` | `/api/admin/analytics/revenue-report` | Revenue and sales analytics |
| `GET` | `/api/admin/analytics/dashboard` | Executive dashboard data |

## 👥 Default User Accounts

The application comes with pre-configured accounts for testing:

| Role | Username | Password | Email | Capabilities |
|------|----------|----------|-------|-------------|
| **Admin** | `admin` | `admin123` | admin@ecommerce.com | Full system access, analytics, user management |
| **User** | `user` | `user123` | user@ecommerce.com | Product browsing, order placement |

## 🗄️ Database Schema

### Core Tables
- **`users`** - User accounts, authentication, and profile information
- **`products`** - Product catalog with inventory tracking and versioning
- **`orders`** - Customer orders with status tracking
- **`order_items`** - Individual items within orders with pricing snapshots

### Key Features
- **Optimistic Locking**: Products use version-based concurrency control
- **Audit Trail**: Created/updated timestamps and user tracking
- **Data Integrity**: Foreign key constraints and check constraints
- **Performance**: Strategic indexing on frequently queried columns

## ⚙️ Configuration

### Application Profiles

| Profile | Purpose | Database | Port | Logging |
|---------|---------|----------|------|---------|
| `local` | Development | PostgreSQL (localhost:5433) | 8080 | DEBUG |
| `docker` | Container deployment | PostgreSQL (container) | 8080 | INFO |
| `test` | Automated testing | H2 (in-memory) | Random | WARN |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `local` | Active Spring profile |
| `JWT_SECRET` | Auto-generated | JWT token signing secret |
| `SPRING_DATASOURCE_URL` | Profile-dependent | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres1234` | Database password |

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Test Categories
- **Unit Tests**: Service layer business logic
- **Integration Tests**: Repository and database operations
- **Security Tests**: Authentication and authorization
- **Container Tests**: Full application stack with Testcontainers

### Test Coverage
The project includes comprehensive tests for:
- Authentication and JWT token handling
- Product CRUD operations and search
- Order processing and inventory management
- Analytics and reporting functionality

## 🏗️ Project Structure

```
src/
├── main/java/com/example/ecommerce/
│   ├── config/              # Configuration classes
│   │   ├── SecurityConfig.java
│   │   └── JwtConfig.java
│   ├── controller/          # REST API controllers
│   │   ├── AuthController.java
│   │   ├── ProductController.java
│   │   ├── OrderController.java
│   │   └── AnalyticsController.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/         # API request DTOs
│   │   └── response/        # API response DTOs
│   ├── exception/           # Exception handling
│   ├── model/               # JPA entities
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── repository/          # Data access layer
│   ├── security/            # Security components
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   ├── service/             # Business logic layer
│   └── ECommerceApplication.java
├── main/resources/
│   ├── application.yml      # Application configuration
│   └── db/changelog/        # Liquibase database migrations
└── test/                    # Test classes
```

## 🐳 Docker Configuration

### Multi-Stage Build
The application uses an optimized multi-stage Dockerfile:
1. **Build Stage**: Gradle build with dependency caching
2. **Runtime Stage**: Lightweight Alpine JRE with security hardening

### Container Features
- **Security**: Non-root user execution
- **Health Checks**: Built-in application health monitoring
- **Resource Management**: JVM container-aware memory settings
- **Networking**: Isolated bridge network for service communication

### Development vs Production
- **Development** (`docker-compose.dev.yml`): Database only on port 5433
- **Production** (`docker-compose.yml`): Full stack with health checks and restart policies

## 🔧 Development Guidelines

### Code Quality
- **Clean Code**: Following SOLID principles and clean architecture
- **Documentation**: Comprehensive JavaDoc and OpenAPI annotations
- **Error Handling**: Centralized exception handling with meaningful error responses
- **Validation**: Input validation using Bean Validation (JSR-303)

### Database Best Practices
- **Migrations**: All schema changes through Liquibase
- **Indexing**: Strategic indexes for query performance
- **Constraints**: Database-level data integrity enforcement
- **Connection Pooling**: HikariCP for optimal connection management

### Security Considerations
- **Authentication**: Stateless JWT tokens with configurable expiration
- **Authorization**: Method-level security with role-based access
- **Password Security**: BCrypt hashing with salt
- **Input Validation**: Protection against injection attacks

## 📈 Performance & Monitoring

### Monitoring Endpoints
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Performance Features
- **Connection Pooling**: Optimized database connections
- **Lazy Loading**: JPA lazy loading with proper session management
- **Batch Processing**: JDBC batch operations for bulk inserts
- **Caching**: Strategic caching for frequently accessed data

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the [API documentation](http://localhost:8080/swagger-ui/index.html) when running locally
- Review the application logs for troubleshooting