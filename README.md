# Product Service Monorepo

A **Spring Boot 3.2.5** microservices monorepo featuring product management with multi-tenant support, event auditing, and Apache Kafka integration.

## Features

✅ **Multi-Service Architecture**: Product Service + Integration Service  
✅ **Multi-Tenancy**: Complete tenant isolation at database level  
✅ **Event Auditing**: Complete audit trail with Kafka event streaming  
✅ **DTO-Based API**: Ensures backward compatibility and clean contracts  
✅ **Exception Standardization**: Unified error response format  
✅ **Code Quality**: SpotBugs, Checkstyle, JaCoCo code coverage  
✅ **Docker Support**: Fully containerized with Docker Compose  
✅ **Database Migrations**: Flyway for schema versioning  

## Quick Start

### Prerequisites

- **Java 21+**
- **Docker & Docker Compose**
- **Maven 3.6+** (for local development)

### Run with Docker Compose

```bash
docker-compose up --build
```

This starts:
- **PostgreSQL** (port 5432)
- **Apache Kafka** (port 9092)
- **Zookeeper** (port 2181)
- **Product Service** (port 8080)
- **Integration Service** (port 8081)

### Run Locally

```bash
mvn spring-boot:run
```

Requires PostgreSQL and Kafka running (configure in `application.yml`)

## API Endpoints

**Base Path**: `/api/products`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List all products |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/{id}/history` | Get product audit history |
| POST | `/api/products` | Create product |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |

**Health Check**: `GET /actuator/health`

## Architecture Highlights

### Multi-Tenancy
- Tenant extracted via HTTP headers in `TenantFilter`
- Thread-local storage in `TenantContext`
- Automatic query filtering by tenant ID
- All entities inherit from `TenantEntity`

### Event-Driven Architecture
- Products events (CREATED, UPDATED, DELETED, LOW_STOCK) published to Kafka
- Complete audit trail accessible via history endpoint
- Integration Service consumes product events

### Clean Code & Stability
- **DTOs**: Request/Response objects prevent entity exposure
- **Mappers**: MapStruct handles entity ↔ DTO conversion
- **Exception Handling**: Standardized `ErrorResponse` format
- **Validation**: Jakarta Bean Validation at DTO layer

## Project Structure

```
product-service/
├── src/main/java/com/example/productservice/
│   ├── controller/       # REST endpoints & exception handlers
│   ├── service/          # Business logic
│   ├── repository/       # Data access with specifications
│   ├── model/            # JPA entities
│   ├── dto/              # Request/Response DTOs
│   ├── mapper/           # MapStruct mappers
│   ├── kafka/            # Event producer/consumer
│   ├── security/         # Multi-tenancy logic
│   ├── filter/           # Tenant extraction filter
│   ├── exception/        # Custom exceptions
│   └── constant/         # Application constants
├── src/test/java/        # Unit & integration tests
├── src/main/resources/
│   ├── application.yml   # Configuration
│   ├── db/migration/     # Flyway migrations
│   └── logback-spring.xml
└── pom.xml              # Maven dependencies & plugins
```

See **[repo.md](repo.md)** for detailed architecture documentation.  
See **[API_CONTRACT.md](API_CONTRACT.md)** for complete API specifications.

## Configuration

### Environment Variables

```bash
# Database
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5432

# Services
PRODUCT_SERVICE_PORT=8080
INTEGRATION_SERVICE_PORT=8081
```

### Local Development

Create `.env` file in project root:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/products_db
    username: postgres
    password: postgres
  kafka:
    bootstrap-servers: localhost:9092
```

## Testing & Quality

```bash
# Run all tests with code coverage
mvn clean test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Full verification (tests + SpotBugs + code coverage)
mvn verify

# Generate coverage report
mvn jacoco:report
# Report: target/site/jacoco/index.html

# Run SpotBugs analysis
mvn spotbugs:check
```

## Building & Deployment

```bash
# Build JAR
mvn clean package

# Docker image is built automatically with docker-compose
docker-compose build

# Push to registry (configure in pom.xml)
docker push your-registry/product-service:latest
```

## Useful Commands

```bash
# View service logs
docker-compose logs -f product-service

# Access PostgreSQL
docker exec -it postgres-db psql -U postgres -d products_db

# Kafka logs
docker-compose logs -f kafka

# Stop all services
docker-compose down

# Clean up volumes (careful!)
docker-compose down -v
```

## Documentation

- **[repo.md](repo.md)**: Comprehensive architecture & development guide
- **[API_CONTRACT.md](API_CONTRACT.md)**: Complete API specification & stability guarantees
- **[docker-compose.yml](docker-compose.yml)**: Service orchestration configuration

## Development Workflow

1. **Create feature branch**: `git checkout -b feature/description`
2. **Make changes** following code conventions
3. **Run tests & checks**: `mvn clean verify`
4. **Commit with clear messages**: `git commit -m "description"`
5. **Push and create pull request**

## License

Proprietary - See LICENSE file
