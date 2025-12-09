# Product Service Repository

## Project Overview

**Product Service** is a Spring Boot 3.2.5 application that manages product catalogs with multi-tenant support, event auditing, and Kafka integration. The service maintains complete product history, implements tenant isolation, and provides a RESTful API for product management.

**Key Characteristics:**
- **Language**: Java 21
- **Framework**: Spring Boot 3.2.5
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Message Broker**: Apache Kafka
- **Architecture**: Microservice with multi-tenancy and audit trail

---

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.6+

### Run with Docker Compose
```bash
docker-compose up --build
```

This starts:
- PostgreSQL on port 5432
- Kafka on port 9092
- Zookeeper on port 2181
- Spring Boot app on port 8080

### Run Locally
```bash
mvn spring-boot:run
```

---

## Project Structure

```
product-service/
├── src/main/java/com/example/productservice/
│   ├── controller/              # REST API endpoints
│   │   ├── ProductController.java
│   │   └── GlobalExceptionHandler.java
│   ├── service/                 # Business logic
│   │   └── ProductService.java
│   ├── repository/              # Data access layer
│   │   └── ProductRepository.java
│   ├── model/                   # JPA entities
│   │   ├── Product.java
│   │   ├── TenantEntity.java
│   │   └── HasTenantId.java
│   ├── dto/                     # Data Transfer Objects
│   │   ├── ProductRequestDTO.java
│   │   ├── ProductResponseDTO.java
│   │   ├── PageResponseDTO.java
│   │   └── ErrorResponse.java
│   ├── mapper/                  # DTO mapping
│   │   └── ProductMapper.java
│   ├── kafka/                   # Event streaming
│   │   ├── ProductProducer.java
│   │   ├── ProductConsumer.java
│   │   ├── KafkaConfig.java
│   │   └── HistoryBuilder.java
│   ├── exception/               # Custom exceptions
│   │   ├── ProductNotFoundException.java
│   │   ├── ValidationException.java
│   │   ├── BusinessException.java
│   │   ├── ConflictException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   └── TenantMissingException.java
│   ├── security/                # Tenant management
│   │   ├── TenantContext.java
│   │   ├── TenantProvider.java
│   │   └── TenantValidator.java
│   ├── filter/                  # Request filters
│   │   └── TenantFilter.java
│   ├── listener/                # Entity listeners
│   │   └── TenantEntityListener.java
│   ├── constant/                # Application constants
│   │   └── AppConstants.java
│   └── ProductServiceApplication.java
├── src/test/java/               # Unit & integration tests
├── src/main/resources/
│   ├── application.yml
│   ├── db/migration/            # Flyway migrations
│   └── logback-spring.xml
├── pom.xml                      # Maven configuration
├── docker-compose.yml           # Docker services
├── Dockerfile                   # Application container
├── checkstyle.xml              # Code style rules
├── spotbugs-exclude.xml        # Bug detection config
├── API_CONTRACT.md             # API specification
└── README.md                   # Getting started guide
```

---

## Core Dependencies

### Spring Boot Starters
- **spring-boot-starter-web**: REST API support
- **spring-boot-starter-data-jpa**: ORM and database access
- **spring-boot-starter-validation**: Bean validation
- **spring-boot-starter-actuator**: Monitoring and health checks
- **spring-boot-devtools**: Development tooling with hot reload

### Data & Database
- **postgresql**: PostgreSQL JDBC driver
- **h2**: In-memory database for testing
- **flyway-core**: Database schema migration

### Message Broker
- **spring-kafka**: Kafka producer/consumer support
- **spring-kafka-test**: Kafka testing utilities

### Utilities
- **lombok**: Code generation (getters, setters, etc.)
- **mapstruct**: DTO mapping code generation

### Testing
- **spring-boot-starter-test**: JUnit 5, Mockito, AssertJ

### Code Quality
- **jacoco-maven-plugin**: Code coverage reporting
- **spotbugs-maven-plugin**: Bug detection analysis
- **maven-checkstyle-plugin**: Code style verification

---

## API Endpoints

**Base Path:** `/api/products`

### Products
| Method | Endpoint | Status | Description |
|--------|----------|--------|-------------|
| GET | `/api/products` | 200 | List all products |
| GET | `/api/products/{id}` | 200/404 | Get product by ID |
| GET | `/api/products/{id}/history` | 200/404 | Get product audit history |
| POST | `/api/products` | 201/400 | Create product |
| PUT | `/api/products/{id}` | 200/400/404 | Update product |
| DELETE | `/api/products/{id}` | 204/404 | Delete product |

### Request/Response DTOs

**ProductRequestDTO** (Create/Update)
```json
{
  "name": "string (1-255)",
  "description": "string (1-2000)",
  "category": "string (1-100)",
  "price": "double (>0, max 2 decimals)",
  "stockQuantity": "integer (>=0)"
}
```

**ProductResponseDTO** (Response)
```json
{
  "id": "long",
  "name": "string",
  "description": "string",
  "category": "string",
  "price": "double",
  "stockQuantity": "integer",
  "eventType": "CREATED|UPDATED|DELETED|LOW_STOCK",
  "eventTime": "LocalDateTime",
  "originalProductId": "long (null for current version)"
}
```

---

## Error Handling

All errors return standardized `ErrorResponse`:

```json
{
  "errorId": "UUID",
  "status": "HTTP status code",
  "error": "ERROR_CODE",
  "message": "Human-readable message",
  "timestamp": "LocalDateTime",
  "path": "/api/products/...",
  "fieldErrors": "Map<String, String> (nullable)",
  "traceId": "Request trace ID (nullable)"
}
```

### Error Codes
- **PRODUCT_NOT_FOUND** (404): Product doesn't exist
- **VALIDATION_ERROR** (400): Input validation failed
- **INVALID_ARGUMENT** (400): Illegal argument
- **CONFLICT** (409): Business logic conflict
- **UNAUTHORIZED** (401): Authentication/authorization failed
- **INTERNAL_SERVER_ERROR** (500): Unexpected error

---

## Architecture Highlights

### Multi-Tenancy
- **Tenant Extraction**: Via `TenantFilter` from request context
- **Isolation**: All queries automatically filtered by tenant ID
- **Entity Base**: `TenantEntity` ensures all entities contain tenant ID
- **Validation**: `TenantValidator` ensures proper tenant context

**Key Classes:**
- `TenantFilter.java`: Extracts tenant from HTTP headers/context
- `TenantContext.java`: Thread-local tenant storage
- `TenantProvider.java`: Retrieves current tenant
- `TenantValidator.java`: Validates tenant authorization

### Event Auditing & Kafka
- **Event Types**: CREATED, UPDATED, DELETED, LOW_STOCK
- **History Tracking**: All product changes stored with metadata
- **Kafka Integration**: Product events published for real-time subscribers
- **History Endpoint**: Complete audit trail accessible via `/history`

**Key Classes:**
- `ProductProducer.java`: Publishes events to Kafka
- `ProductConsumer.java`: Consumes product events
- `HistoryBuilder.java`: Constructs event objects
- `KafkaConfig.java`: Kafka topic and consumer group configuration

### DTO-Based API
- **Separation of Concerns**: Entities never directly exposed
- **Stability**: Ensures backward compatibility
- **Validation**: Input validation at DTO layer
- **Mapping**: MapStruct handles entity ↔ DTO conversion

**Key Classes:**
- `ProductRequestDTO.java`: Validates create/update input
- `ProductResponseDTO.java`: Formats API responses
- `ProductMapper.java`: MapStruct mapper implementation

### Exception Handling
- **Standardized Responses**: All exceptions converted to `ErrorResponse`
- **Exception Hierarchy**: Custom exception classes for different scenarios
- **Global Handler**: `GlobalExceptionHandler` catches and formats exceptions

**Exception Classes:**
- `ProductNotFoundException`: Extends `ResourceNotFoundException`
- `ValidationException`: For validation failures
- `BusinessException`: For business logic violations
- `ConflictException`: For data conflicts (409)
- `UnauthorizedException`: For auth failures (401)
- `TenantMissingException`: For missing tenant context

---

## Build & Quality Tools

### Maven Plugins
- **maven-compiler-plugin**: Java 21 compilation with annotation processors
- **spring-boot-maven-plugin**: Application packaging and run
- **jacoco-maven-plugin**: Code coverage analysis
- **spotbugs-maven-plugin**: Static bug detection (effort=Max, threshold=High)
- **maven-checkstyle-plugin**: Code style verification (disabled due to Guava compatibility)

### Run Quality Checks
```bash
mvn clean test                  # Run tests with coverage
mvn spotbugs:check             # Run SpotBugs analysis
mvn checkstyle:check           # Run Checkstyle (manual)
mvn verify                      # Full verification including all checks
```

### Code Coverage
- Generated by JaCoCo during `test` phase
- Report: `target/site/jacoco/index.html`

---

## Environment Configuration

### Docker Compose Environment Variables
- `SPRING_DATASOURCE_URL`: PostgreSQL connection URL
- `SPRING_DATASOURCE_USERNAME`: Database user
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers

### Local Development
Create `.env` file or set in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/productdb
    username: postgres
    password: postgres
  kafka:
    bootstrap-servers: localhost:9092
```

---

## Database Schema

### Product Table
- `id`: Primary key (Long)
- `tenant_id`: Tenant identifier (String)
- `name`: Product name (1-255 chars)
- `description`: Product description (1-2000 chars)
- `category`: Product category (1-100 chars)
- `price`: Product price (Decimal 12,2)
- `stock_quantity`: Available stock (Integer)
- `event_type`: CREATED|UPDATED|DELETED|LOW_STOCK (String)
- `event_time`: Event timestamp (Timestamp)
- `original_product_id`: Reference to original product (Long, nullable)
- `created_at`: Record creation time (Timestamp)
- `updated_at`: Last update time (Timestamp)

### Migration
- **Tool**: Flyway
- **Location**: `src/main/resources/db/migration/`
- **Pattern**: `V001__initial_schema.sql`, etc.

---

## Key Features

✅ **Product Management**: Full CRUD operations with validation  
✅ **Multi-Tenancy**: Complete tenant isolation  
✅ **Audit Trail**: Complete product change history  
✅ **Event Streaming**: Kafka integration for real-time events  
✅ **Exception Handling**: Standardized error responses  
✅ **API Contract**: Backward compatibility guarantees  
✅ **Code Quality**: SpotBugs, Checkstyle, JaCoCo coverage  
✅ **Testing**: Comprehensive unit & integration tests  
✅ **Docker Support**: Easy containerization and deployment  

---

## Development Workflow

### 1. Create Feature Branch
```bash
git checkout -b feature/new-feature
```

### 2. Make Changes
- Follow existing code conventions
- Ensure no commented-out code
- Follow the DTO layer pattern

### 3. Run Tests & Quality Checks
```bash
mvn clean verify
```

### 4. Commit & Push
```bash
git add .
git commit -m "description of changes"
git push origin feature/new-feature
```

### 5. Create Pull Request
- Reference issue/ticket
- Describe changes
- Ensure all checks pass

---

## Useful Commands

```bash
# Build project
mvn clean package

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run with Docker Compose
docker-compose up --build

# View Kafka logs
docker-compose logs -f kafka

# Access PostgreSQL
docker exec -it postgres-container psql -U postgres -d productdb

# Run SpotBugs
mvn spotbugs:gui

# Generate code coverage report
mvn jacoco:report

# Hot reload during development
mvn spring-boot:run
```

---

## API Documentation

See `API_CONTRACT.md` for comprehensive API specifications including:
- Detailed endpoint documentation
- Request/response examples
- Validation rules
- Error scenarios
- Multitenancy details
- Audit trail tracking
- API stability guarantees

---

## Related Documentation

- **API_CONTRACT.md**: Complete API specification and backward compatibility guidelines
- **README.md**: Quick start guide
- **pom.xml**: Dependency management and build configuration
- **docker-compose.yml**: Service orchestration

---

## Version History

| Version | Date | Notes |
|---------|------|-------|
| 0.0.1-SNAPSHOT | Current | Initial implementation with multi-tenant support |

---

## License

Proprietary - See LICENSE file

---

## Contact & Support

For issues and questions, please refer to the project's issue tracker or internal documentation.
