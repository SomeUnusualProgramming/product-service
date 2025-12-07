# Product Service Repository

## Project Overview

A **Spring Boot microservice** for managing products with PostgreSQL database, Apache Kafka for event streaming, and multi-tenant support. The application provides REST endpoints to manage products, tracks audit history, and integrates with Kafka for asynchronous product events.

**Project Version**: 0.0.1-SNAPSHOT  
**Java Version**: 21  
**Spring Boot Version**: 3.2.5

## Tech Stack

- **Framework**: Spring Boot 3.2.5
- **Language**: Java 21
- **Database**: PostgreSQL 15
- **Message Broker**: Apache Kafka (with Zookeeper)
- **Build Tool**: Maven 3.9
- **ORM**: JPA/Hibernate
- **Database Migrations**: Flyway
- **Mapping**: MapStruct 1.5.5
- **Additional Libraries**: Lombok 1.18.42, Spring DevTools

## Project Structure

```
src/
├── main/
│   ├── java/com/example/productservice/
│   │   ├── ProductServiceApplication.java              # Main Spring Boot entry point
│   │   ├── constant/
│   │   │   └── AppConstants.java                       # Application constants
│   │   ├── controller/
│   │   │   ├── ProductController.java                  # REST endpoints for product management
│   │   │   └── GlobalExceptionHandler.java             # Centralized exception handling
│   │   ├── service/
│   │   │   └── ProductService.java                     # Business logic layer
│   │   ├── repository/
│   │   │   └── ProductRepository.java                  # Database access (JPA)
│   │   ├── model/
│   │   │   ├── Product.java                            # Product entity
│   │   │   ├── TenantEntity.java                       # Base entity with tenant isolation
│   │   │   ├── HasTenantId.java                        # Tenant interface
│   │   ├── dto/
│   │   │   ├── ProductRequestDTO.java                  # Request DTO for product creation/update
│   │   │   ├── ProductResponseDTO.java                 # Response DTO with audit information
│   │   │   ├── ProductDTO.java                         # Data transfer object
│   │   │   └── ErrorResponse.java                      # Standardized error response
│   │   ├── mapper/
│   │   │   └── ProductMapper.java                      # MapStruct mapper for DTOs
│   │   ├── kafka/
│   │   │   ├── KafkaConfig.java                        # Kafka configuration
│   │   │   ├── ProductProducer.java                    # Kafka message producer
│   │   │   ├── ProductConsumer.java                    # Kafka message consumer
│   │   │   └── HistoryBuilder.java                     # Audit history builder
│   │   ├── exception/
│   │   │   ├── BusinessException.java                  # Business logic exceptions
│   │   │   ├── ConflictException.java                  # Conflict exceptions (409)
│   │   │   ├── ProductNotFoundException.java           # Product not found exception
│   │   │   ├── ResourceNotFoundException.java          # Generic resource not found
│   │   │   ├── TenantMissingException.java             # Tenant ID missing exception
│   │   │   ├── UnauthorizedException.java              # Authorization exceptions
│   │   │   └── ValidationException.java                # Validation exceptions
│   │   ├── filter/
│   │   │   └── TenantFilter.java                       # Tenant ID extraction from requests
│   │   ├── listener/
│   │   │   └── TenantEntityListener.java               # JPA listener for tenant filtering
│   │   ├── security/
│   │   │   ├── TenantContext.java                      # ThreadLocal tenant context
│   │   │   ├── TenantProvider.java                     # Tenant ID provider
│   │   │   └── TenantValidator.java                    # Tenant validation
│   │   └── resources/
│   │       ├── application.properties                  # Application configuration
│   │       ├── application-railway.properties          # Railway.app deployment config
│   │       └── db/migration/
│   │           ├── V1__Initial_schema.sql              # Initial schema
│   │           ├── V2__Add_tenant_id_to_product.sql   # Add tenant ID column
│   │           └── V3__Add_created_at_to_product.sql  # Add audit timestamp
│   └── test/
│       └── resources/
│           └── application-test.properties             # Test configuration
├── docker-compose.yml                                   # Docker container orchestration
├── Dockerfile                                           # Docker image definition
├── API_CONTRACT.md                                      # Detailed API contract
└── pom.xml                                              # Maven project configuration
```

## Key Features

### Multi-Tenancy Support
- All products are isolated by tenant ID
- Tenant context extracted from HTTP headers
- Automatic tenant filtering on all queries
- TenantFilter extracts tenant ID from X-Tenant-ID header

### Audit Trail & Event History
- All products maintain complete change history
- Event types: CREATED, UPDATED, DELETED, LOW_STOCK
- Original product ID tracked for historical records
- Accessible via `/api/products/{id}/history` endpoint
- Event timestamps recorded for all changes

### Error Handling
- Centralized exception handling via GlobalExceptionHandler
- Standardized ErrorResponse format with:
  - Unique errorId (UUID) for error tracking
  - HTTP status codes
  - Field-level validation errors
  - Request trace ID for correlation
- Custom exception hierarchy for business logic

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | Retrieve all products (tenant-scoped) |
| GET | `/api/products/{id}` | Retrieve product by ID |
| GET | `/api/products/{id}/history` | Get product change history |
| POST | `/api/products` | Create a new product |
| PUT | `/api/products/{id}` | Update existing product |
| DELETE | `/api/products/{id}` | Delete product by ID |

## Database Configuration

**Connection Details:**
- **Host**: PostgreSQL container (`db:5432` in Docker)
- **Database**: `products`
- **Username**: `postgres`
- **Password**: `haslo`
- **Dialect**: PostgreSQL
- **DDL Auto**: `validate` (Flyway manages schema)
- **Migration Tool**: Flyway (V1, V2, V3 migrations)

**Database Schema:**
- `product` table with columns:
  - id (Long, primary key)
  - name (String, 255 chars)
  - description (String, 2000 chars)
  - category (String, 100 chars)
  - price (Decimal, 10 integer + 2 decimal)
  - stock_quantity (Integer)
  - tenant_id (String, multi-tenancy isolation)
  - event_type (String, audit tracking)
  - event_time (LocalDateTime, audit timestamp)
  - original_product_id (Long, historical references)

## Kafka Configuration

**Bootstrap Servers**: `kafka:9092`  
**Zookeeper**: `zookeeper:2181`  
**Auto-create Topics**: Enabled  

**Topics:**
- `product-events`: Product lifecycle events (CREATED, UPDATED, DELETED)

**Serialization:**
- Producer: StringSerializer (key and value)
- Consumer: StringDeserializer (key and value)

**Consumer Settings:**
- Consumer group: `product-group`
- Auto offset reset: earliest

## DTO Specifications

### ProductRequestDTO
Used for POST and PUT requests.

| Field | Type | Constraints | Required |
|-------|------|-----------|----------|
| name | String | 1-255 chars | Yes |
| description | String | 1-2000 chars | Yes |
| category | String | 1-100 chars | Yes |
| price | Double | > 0, max 2 decimals | Yes |
| stockQuantity | Integer | >= 0 | Yes |

### ProductResponseDTO
Returned in all successful responses. Includes audit information.

| Field | Type | Notes |
|-------|------|-------|
| id | Long | Product identifier |
| name | String | Product name |
| description | String | Product description |
| category | String | Product category |
| price | Double | Product price |
| stockQuantity | Integer | Available stock |
| eventType | String | CREATED, UPDATED, DELETED, LOW_STOCK |
| eventTime | LocalDateTime | Event timestamp |
| originalProductId | Long | Parent product ID (null for current versions) |

### ErrorResponse
| Field | Type | Notes |
|-------|------|-------|
| errorId | String | UUID for error tracking |
| status | Integer | HTTP status code |
| error | String | Error code |
| message | String | Human-readable error message |
| timestamp | LocalDateTime | Error occurrence time |
| path | String | Request URI |
| fieldErrors | Map | Field-specific validation errors |
| traceId | String | Request trace ID |

## Exception Hierarchy

```
Exception
├── ResourceNotFoundException
│   └── ProductNotFoundException
├── ValidationException
├── BusinessException
├── ConflictException
├── UnauthorizedException
└── TenantMissingException
```

## Error Codes

| Error Code | HTTP Status | Description |
|-----------|------------|-------------|
| PRODUCT_NOT_FOUND | 404 | Requested product does not exist |
| RESOURCE_NOT_FOUND | 404 | Generic resource not found |
| VALIDATION_ERROR | 400 | Input validation failed |
| INVALID_ARGUMENT | 400 | Illegal argument provided |
| CONFLICT | 409 | Business logic conflict |
| UNAUTHORIZED | 401 | Authentication/authorization failed |
| METHOD_NOT_ALLOWED | 405 | HTTP method not allowed |
| ENDPOINT_NOT_FOUND | 404 | Endpoint does not exist |
| INTERNAL_SERVER_ERROR | 500 | Unexpected server error |

## Running the Application

### Prerequisites
- Docker
- Docker Compose
- Maven 3.9+
- Java 21+

### Using Docker Compose

```bash
docker-compose up --build
```

This starts:
- **PostgreSQL**: Port 5432
- **Zookeeper**: Port 2181
- **Kafka**: Port 9092
- **Spring Boot App**: Port 8080

### Local Development (without Docker)

```bash
mvn clean install
mvn spring-boot:run
```

Requires PostgreSQL and Kafka running locally.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_DB` | products | PostgreSQL database name |
| `POSTGRES_USER` | postgres | PostgreSQL username |
| `POSTGRES_PASSWORD` | haslo | PostgreSQL password |
| `DB_PORT` | 5432 | PostgreSQL port |
| `APP_PORT` | 8080 | Spring Boot application port |
| `SPRING_DATASOURCE_URL` | jdbc:postgresql://db:5432/products | Database connection URL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | kafka:9092 | Kafka bootstrap servers |

## Dependencies

### Core
- `spring-boot-starter-web`: REST API framework
- `spring-boot-starter-data-jpa`: ORM and database abstraction
- `spring-boot-starter-validation`: Bean validation (Jakarta)
- `spring-boot-devtools`: Hot reloading during development
- `spring-boot-starter-test`: Testing framework

### Database
- `postgresql`: PostgreSQL JDBC driver
- `h2`: In-memory database for testing
- `flyway-core`: Database migration management

### Messaging
- `spring-kafka`: Kafka integration
- `spring-kafka-test`: Kafka testing utilities

### Utilities
- `lombok` (1.18.42): Boilerplate code generation
- `mapstruct` (1.5.5): Type-safe DTO mapping

## Build & Deployment

**Build Tool**: Maven  
**Build Command**: `mvn clean package`  
**Output JAR**: `target/product-service-0.0.1-SNAPSHOT.jar`  
**Docker Base Image**: `maven:3.9.11-eclipse-temurin-17`  
**Exposed Port**: 8080  

### Docker Build Process
1. Copy pom.xml
2. Copy source code
3. Run Maven clean package (tests skipped in Docker build)
4. Run JAR file with exposed port 8080

## Development Notes

- **Lombok**: Reduces boilerplate with auto-generated getters, setters, constructors, and log fields
- **MapStruct**: Type-safe DTO mapping with compile-time code generation
- **Flyway**: Version-controlled database migrations
- **Kafka**: Asynchronous event streaming for product changes
- **Multi-tenancy**: All operations automatically scoped to tenant context
- **Audit Trail**: Complete change history maintained for all products
- **Exception Handling**: Centralized, standardized error responses
- **DevTools**: Enables hot reloading for faster development cycles

## API Stability

See [API_CONTRACT.md](API_CONTRACT.md) for detailed:
- Backward compatibility guarantees
- Breaking change prevention
- DTO specifications
- Validation rules
- Complete API documentation
