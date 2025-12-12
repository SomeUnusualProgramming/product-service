# Product Service Monorepo - Architecture & Development Guide

## Overview

This is a **Spring Boot 3.2.5** microservices monorepo featuring:
- Multi-service architecture (Product Service + Integration Service)
- Complete multi-tenant isolation at database level
- Event-driven architecture with Apache Kafka
- Comprehensive audit trail functionality
- Clean code principles with DTOs and exception standardization
- Enterprise-grade code quality tools (SpotBugs, Checkstyle, JaCoCo)
- Full containerization with Docker Compose

**Java Version**: 21+  
**Build Tool**: Maven 3.6+  
**Framework**: Spring Boot 3.2.5

---

## Repository Structure

```
product-service/
├── product-service/              # Core product management microservice
│   ├── src/main/java/com/example/productservice/
│   │   ├── controller/           # REST endpoints & global exception handlers
│   │   ├── service/              # Business logic & domain operations
│   │   ├── repository/           # JPA repositories with specifications
│   │   ├── model/                # JPA entities with tenant isolation
│   │   ├── dto/                  # Request/Response DTOs
│   │   ├── mapper/               # MapStruct entity ↔ DTO mappers
│   │   ├── kafka/                # Event producer/consumer implementation
│   │   ├── security/             # Multi-tenancy logic & TenantContext
│   │   ├── filter/               # Tenant extraction from HTTP headers
│   │   ├── exception/            # Custom exception classes
│   │   └── constant/             # Application constants
│   ├── src/test/java/            # Unit & integration tests
│   ├── src/main/resources/
│   │   ├── application.yml       # Service configuration
│   │   ├── db/migration/         # Flyway database migrations
│   │   └── logback-spring.xml    # Logging configuration
│   ├── Dockerfile                # Container build definition
│   └── pom.xml
│
├── integration-service/          # Data integration microservice
│   ├── src/main/java/com/example/integrationservice/
│   │   ├── controller/           # REST endpoints
│   │   ├── service/              # Integration business logic
│   │   ├── repository/           # Data access layer
│   │   ├── model/                # JPA entities
│   │   ├── dto/                  # Request/Response DTOs
│   │   ├── mapper/               # MapStruct mappers
│   │   ├── kafka/                # Event consumer for product events
│   │   ├── exception/            # Custom exceptions
│   │   └── config/               # Service configuration
│   ├── src/test/java/            # Unit & integration tests
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── db/migration/
│   │   └── logback-spring.xml
│   ├── Dockerfile
│   └── pom.xml
│
├── k8s/                          # Kubernetes deployment manifests
│   ├── namespace.yaml
│   ├── configmap-secret.yaml
│   ├── product-service-deployment.yaml
│   ├── integration-service-deployment.yaml
│   ├── postgres-statefulset.yaml
│   ├── kafka-statefulset.yaml
│   ├── zookeeper-deployment.yaml
│   ├── ingress.yaml
│   ├── kustomization.yaml
│   ├── deploy.sh
│   └── delete.sh
│
├── .github/workflows/            # CI/CD pipelines
├── .env                          # Environment variables (local)
├── DB.env                        # Database configuration
├── docker-compose.yml            # Local development infrastructure
├── init-databases.sql            # Database initialization script
├── init-ollama.sh                # Ollama setup script
├── pom.xml                       # Parent POM (modules aggregator)
├── checkstyle.xml                # Code style configuration
├── spotbugs-exclude.xml          # SpotBugs exclusions
├── lombok.config                 # Lombok configuration
├── README.md                     # Quick start guide
├── API_CONTRACT.md               # Complete API specification
└── mvnw / mvnw.cmd              # Maven wrapper scripts
```

---

## Technology Stack

### Core Framework
| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.2.5 | Web framework & microservice foundation |
| Spring Data JPA | 3.2.5 | Database access & ORM |
| Spring Kafka | 3.2.5 | Event streaming integration |
| Spring Validation | 3.2.5 | DTO & model validation |
| Spring Actuator | 3.2.5 | Health checks & metrics |

### Database & Messaging
| Component | Version | Purpose |
|-----------|---------|---------|
| PostgreSQL | 15 | Relational database |
| Apache Kafka | Latest | Event-driven messaging |
| Flyway | Latest | Database schema versioning |

### Development & Code Quality
| Component | Version | Purpose |
|-----------|---------|---------|
| Lombok | 1.18.42 | Boilerplate reduction |
| MapStruct | 1.5.5 | Type-safe DTO mapping |
| JaCoCo | 0.8.11 | Code coverage measurement |
| SpotBugs | 4.8.3.1 | Bug detection analysis |
| Checkstyle | 3.3.1 | Code style enforcement |

### Testing
| Component | Purpose |
|-----------|---------|
| JUnit 5 | Unit testing framework |
| Mockito | Mocking & verification |
| Spring Test | Integration testing |
| H2 Database | In-memory testing database |
| Kafka Test | Kafka testing utilities |

### Data Formats (Integration Service)
| Component | Purpose |
|-----------|---------|
| Jackson Databind | JSON processing |
| Jackson XML | XML processing |
| Apache Commons CSV | CSV parsing |

### Containerization
| Component | Purpose |
|-----------|---------|
| Docker | Container runtime |
| Docker Compose | Local orchestration |

---

## Architecture Highlights

### Multi-Tenancy

**Tenant Isolation Strategy**: Database-level tenant isolation with application-level enforcement

**Flow**:
1. `TenantFilter` extracts tenant ID from HTTP header (`X-Tenant-ID`)
2. `TenantContext` stores tenant ID in thread-local storage
3. All JPA queries are automatically filtered by tenant ID
4. All entities inherit from `TenantEntity` base class with `tenantId` field
5. Service layer enforces tenant-aware operations

**Key Components**:
- `TenantFilter`: Extracts & validates tenant context from requests
- `TenantContext`: Thread-local storage for tenant ID
- `TenantEntity`: Base class for all tenant-aware entities
- `TenantSpecification`: JPA specifications with tenant filtering

### Event-Driven Architecture

**Product Events Flowing Through System**:
```
Product Service (Event Producer)
    ↓
    ├── CREATED
    ├── UPDATED
    ├── DELETED
    └── LOW_STOCK
    ↓
    Apache Kafka Topic: product-events
    ↓
    Integration Service (Event Consumer)
```

**Event Publisher** (`ProductEventProducer`):
- Publishes events to Kafka topic `product-events`
- Captures product lifecycle events
- JSON serialization for compatibility

**Event Consumer** (Integration Service):
- Consumes product events from Kafka
- Updates denormalized integration data
- Handles event reconciliation

**Complete Audit Trail**:
- All product events stored in `audit_logs` table
- Accessible via `/api/products/{id}/history` endpoint
- Timestamps & tenant isolation included

### DTO-Based API

**Design Benefits**:
- ✅ Backward compatibility - internal schema changes don't break clients
- ✅ Clean contracts - explicit request/response shapes
- ✅ Security - prevents accidental entity field exposure
- ✅ Validation - DTO-level validation via Jakarta Bean Validation

**Mapping Layer**:
- MapStruct generates type-safe DTO ↔ Entity converters
- Zero runtime reflection overhead
- Compile-time error checking

### Exception Handling

**Standardized Error Response**:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Product not found",
  "message": "Product with ID 123 does not exist",
  "path": "/api/products/123"
}
```

**Exception Hierarchy**:
- `ResourceNotFoundException` → 404
- `BadRequestException` → 400
- `TenantViolationException` → 403
- `InternalServerException` → 500

**Global Exception Handler**: `GlobalExceptionHandler` in controller layer

---

## Infrastructure & Services

### Docker Compose Services

```yaml
Services:
├── PostgreSQL (port 5432)
│   └── Stores: products_db, integration_db
├── Zookeeper (port 2181)
│   └── Kafka coordination
├── Apache Kafka (port 9092)
│   └── Topic: product-events
├── Ollama (port 11434)
│   └── AI/ML model inference
├── Product Service (port 8080)
│   ├── Database: products_db
│   └── Health: /actuator/health
└── Integration Service (port 8081)
    ├── Database: integration_db
    └── Health: /actuator/health
```

### Database Schema

**Product Service** (`products_db`):
- `products` - Product entities with tenant isolation
- `audit_logs` - Complete audit trail of all changes
- `flyway_schema_history` - Migration tracking

**Integration Service** (`integration_db`):
- Integration-specific schemas (denormalized data)
- Event tracking tables
- Flyway migration history

**Multi-Tenancy**: All tables include `tenant_id` column for data isolation

---

## Running the Services

### Option 1: Docker Compose (Recommended)

**Start all services**:
```bash
docker-compose up --build
```

**Service Availability**:
- Product Service: http://localhost:8080
- Integration Service: http://localhost:8081
- PostgreSQL: localhost:5432
- Kafka: localhost:9092
- Ollama: http://localhost:11434

**Stop services**:
```bash
docker-compose down
```

**Clean up volumes** (careful - deletes data):
```bash
docker-compose down -v
```

### Option 2: Local Development

**Prerequisites**:
- Java 21+
- Maven 3.6+
- PostgreSQL running on localhost:5432
- Apache Kafka running on localhost:9092

**Build**:
```bash
mvn clean package
```

**Run Product Service**:
```bash
cd product-service
mvn spring-boot:run
```

**Run Integration Service**:
```bash
cd integration-service
mvn spring-boot:run
```

### Database Configuration

**Environment Variables** (.env or system):
```bash
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5432
PRODUCT_SERVICE_PORT=8080
INTEGRATION_SERVICE_PORT=8081
```

**Local Application Configuration** (application.yml):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/products_db
    username: postgres
    password: postgres
  kafka:
    bootstrap-servers: localhost:9092
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Kafka Topics

**Auto-created Topics** (KAFKA_AUTO_CREATE_TOPICS_ENABLE: true):
- `product-events` - All product lifecycle events

**Topic Configuration**:
- Replication Factor: 1 (configurable for production)
- Partitions: 1 (default)
- Retention: Broker default

---

## API Specification

**Base Paths**:
- Product Service: `/api/products`
- Integration Service: `/api/integration`

### Product Service Endpoints

| Method | Endpoint | Description | Query Params |
|--------|----------|-------------|--------------|
| GET | `/api/products` | List all products | `page`, `size`, `sort` |
| GET | `/api/products/{id}` | Get product by ID | — |
| GET | `/api/products/{id}/history` | Get product audit history | `page`, `size` |
| POST | `/api/products` | Create product | — |
| PUT | `/api/products/{id}` | Update product | — |
| DELETE | `/api/products/{id}` | Delete product | — |

**Health Check**:
- `GET /actuator/health` - Service health status
- `GET /actuator/health/liveness` - Liveness probe
- `GET /actuator/health/readiness` - Readiness probe

**Required Headers**:
- `X-Tenant-ID` - Tenant identifier (all API requests)

**Response Format**: JSON (application/json)

See **[API_CONTRACT.md](API_CONTRACT.md)** for complete API specifications, examples, and stability guarantees.

---

## Code Quality & Testing

### Testing Strategy

**Test Types**:
1. **Unit Tests** - Business logic & service layer
2. **Integration Tests** - API endpoints with H2 database
3. **Kafka Tests** - Message producer/consumer verification

**Testing Stack**:
- JUnit 5 for test framework
- Mockito for mocking
- Spring Test for integration testing
- H2 in-memory database for isolation

### Running Tests

**Run all tests with coverage**:
```bash
mvn clean test
```

**Run specific test class**:
```bash
mvn test -Dtest=ProductServiceTest
```

**Full verification** (tests + analysis + coverage):
```bash
mvn verify
```

**Generate coverage report**:
```bash
mvn jacoco:report
# Report: target/site/jacoco/index.html
```

**Coverage Goals**:
- Line Coverage: ≥ 80%
- Branch Coverage: ≥ 75%
- Excludes: DTOs, entities, configuration classes

### Code Analysis

**SpotBugs** (Bug detection):
```bash
mvn spotbugs:check
```

**Configuration**: High effort, High threshold  
**Exclusions**: `spotbugs-exclude.xml`

**Checkstyle** (Code style):
Integrated with build, enforces Google Java Style Guide (with modifications)

**Configuration**: `checkstyle.xml`

### Build Plugins

**Maven Plugins**:
- `spring-boot-maven-plugin` - Create executable JARs
- `maven-compiler-plugin` - Java 21 compilation
- `jacoco-maven-plugin` - Code coverage (0.8.11)
- `spotbugs-maven-plugin` - Bug analysis (4.8.3.1)

---

## Building & Deployment

### Local Build

**Build JAR files**:
```bash
mvn clean package
```

**Build without tests**:
```bash
mvn clean package -DskipTests
```

**Build specific module**:
```bash
mvn clean package -pl product-service
```

### Docker Build

**Build all images**:
```bash
docker-compose build
```

**Build specific service**:
```bash
docker-compose build product-service
```

**Docker Images**:
- `microservices:product-service:0.0.1-SNAPSHOT`
- `microservices:integration-service:0.0.1-SNAPSHOT`

### Dockerfile Details

**Multi-stage Build**:
1. **Build Stage**: Maven compiles code, runs tests
2. **Runtime Stage**: Slim JRE 21, copies JAR from build

**Optimization**:
- Non-root user execution
- Health checks configured
- Environment variable pass-through

### Kubernetes Deployment

**Deployment Files** (k8s/):
- Namespace configuration
- Service deployments with replicas
- StatefulSets for PostgreSQL & Kafka
- ConfigMap/Secrets for configuration
- Ingress routing

**Deploy to Kubernetes**:
```bash
./k8s/deploy.sh
```

**Remove from Kubernetes**:
```bash
./k8s/delete.sh
```

---

## Development Workflow

### Setting Up Local Environment

1. **Clone repository**:
   ```bash
   git clone <repo-url>
   cd product-service
   ```

2. **Install dependencies**:
   ```bash
   mvn dependency:download-sources
   mvn dependency:resolve
   ```

3. **Start infrastructure**:
   ```bash
   docker-compose up -d db kafka zookeeper ollama
   ```

4. **Create databases** (if needed):
   ```bash
   docker exec -it postgres-db psql -U postgres -f init-databases.sql
   ```

5. **Run services**:
   ```bash
   # Terminal 1
   cd product-service && mvn spring-boot:run
   
   # Terminal 2
   cd integration-service && mvn spring-boot:run
   ```

### Creating a Feature

1. **Create feature branch**:
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Implement feature** following code conventions:
   - DTO-first API design
   - Entity → DTO mapping via MapStruct
   - Service layer business logic
   - Repository with JPA Specifications
   - Integration tests for new functionality

3. **Run tests & checks**:
   ```bash
   mvn clean verify
   ```

4. **Fix any issues**:
   - SpotBugs warnings → `spotbugs-exclude.xml` or fix code
   - Checkstyle violations → Fix formatting
   - Test coverage → Increase test coverage
   - Code coverage < 80% → Add tests

5. **Commit with clear message**:
   ```bash
   git commit -m "feat: add product filtering by category"
   ```

6. **Push & create PR**:
   ```bash
   git push origin feature/my-feature
   ```

### Code Conventions

**Package Structure**:
```
com.example.productservice/
├── controller/     REST endpoints
├── service/        Business logic (interfaces + implementations)
├── repository/     Data access
├── model/          JPA entities
├── dto/            Request/Response objects
├── mapper/         MapStruct mappers
├── kafka/          Event handling
├── security/       Multi-tenancy
├── exception/      Custom exceptions
└── constant/       Constants & enums
```

**Naming**:
- Classes: PascalCase (ProductService, ProductRepository)
- Methods: camelCase (getProductById, createProduct)
- Constants: UPPER_SNAKE_CASE (DEFAULT_PAGE_SIZE)
- Database columns: snake_case (product_id, created_at)

**Lombok Annotations**:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
public class Product {
    ...
}
```

**Validation** (DTOs):
```java
@NotNull
@NotBlank
@Positive
@DecimalMin("0.0")
@Size(min = 3, max = 255)
@Email
@Pattern(regexp = "...")
```

**Exception Handling**:
```java
if (product == null) {
    throw new ResourceNotFoundException("Product not found");
}
```

**Documentation & Comments**:
- Public methods require Javadoc comments
- Method parameters should be documented in `@param` tags
- Return values documented in `@return` tags
- Exceptions documented in `@throws` tags
- Complex logic requires inline comments
- Example:
```java
/**
 * Retrieves a product by its ID for the current tenant.
 *
 * @param id the product ID
 * @return the product if found
 * @throws ProductNotFoundException if the product is not found
 */
public Product getProductById(final Long id) {
    String tenantId = TenantProvider.getCurrentTenantId();
    return productRepository.findByIdAndTenantId(id, tenantId)
        .orElseThrow(() -> new ProductNotFoundException(id));
}
```

**Code Style**:
- Max line length: 120 characters
- Method parameters: Use `final` keyword for immutability
- Import order: static, then standard Java, then Spring/application packages
- No trailing whitespace
- File must end with newline

---

## Useful Commands

### Docker Commands

```bash
# View running containers
docker-compose ps

# View service logs
docker-compose logs -f product-service
docker-compose logs -f kafka

# Access PostgreSQL
docker exec -it postgres-db psql -U postgres -d products_db
\dt                                    -- List tables
SELECT * FROM products WHERE tenant_id = 'tenant1';

# Kafka consumer
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic product-events \
  --from-beginning
```

### Maven Commands

```bash
# Dependency tree
mvn dependency:tree

# Update dependencies
mvn versions:display-dependency-updates

# Clean build
mvn clean install

# Skip tests
mvn clean package -DskipTests

# Run single test
mvn test -Dtest=ProductServiceTest#testCreateProduct

# Debug mode
mvn -X clean verify
```

### Useful Endpoints

```bash
# Product Service (UI)
open http://localhost:8080

# Product Service (API)
curl -H "X-Tenant-Id: tenant1" http://localhost:8080/api/products

# Health check
curl http://localhost:8080/actuator/health

# Actuator metrics
curl http://localhost:8080/actuator/metrics

# Integration Service Health
curl http://localhost:8081/actuator/health
```

---

## Monitoring & Observability

### Health Checks

**Product Service Health**:
```
GET http://localhost:8080/actuator/health
```

**Response**:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "kafka": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

### Metrics

**Available Endpoints**:
- `/actuator/metrics` - All metrics
- `/actuator/metrics/jvm.memory.used` - JVM memory
- `/actuator/metrics/http.server.requests` - Request metrics

### Logging

**Configuration**: `src/main/resources/logback-spring.xml`

**Log Levels**:
```yaml
logging:
  level:
    root: INFO
    com.example.productservice: DEBUG
    org.springframework.kafka: WARN
    org.hibernate: WARN
```

### Audit Trail

**Access Product History**:
```bash
GET http://localhost:8080/api/products/{id}/history
```

**Returns**: Chronological list of all changes with timestamps

---

## Troubleshooting

### Common Issues

**PostgreSQL Connection Failed**:
1. Check container is running: `docker-compose ps`
2. Verify environment variables in docker-compose.yml
3. Check logs: `docker-compose logs db`

**Kafka Connection Issues**:
1. Ensure Zookeeper started first (health check waits)
2. Verify KAFKA_ADVERTISED_LISTENERS = PLAINTEXT://kafka:9092
3. Check Kafka logs: `docker-compose logs kafka`

**Application Port Already in Use**:
1. Change port in docker-compose.yml or .env
2. Or kill process: `lsof -i :8080` then `kill -9 <PID>`

**Tests Failing with H2 Database**:
1. Clear target directory: `mvn clean`
2. H2 schema differences - update test configuration
3. Check test @Configuration classes override main config

**SpotBugs False Positives**:
1. Add exclusion to `spotbugs-exclude.xml`
2. Or add `@SuppressFBWarnings("...")` annotation
3. Document reason for exclusion

---

## Security Considerations

### Multi-Tenancy Security

✅ **Implemented**:
- Tenant ID extracted from HTTP header
- Thread-local storage prevents cross-tenant leaks
- All queries filtered by tenant ID
- Database-level isolation

⚠️ **Important**:
- X-Tenant-ID header should be set by gateway/API (not client)
- Validate tenant ID format & authorization
- Regularly audit tenant data access

### Password & Secrets

**Storage**:
- Sensitive configs in environment variables
- Use Kubernetes Secrets for production
- Never commit .env files or credentials

**Database Passwords**:
- Use strong passwords in production
- Rotate periodically
- Use managed database services in cloud

### SQL Injection Prevention

✅ **Mitigated by**:
- JPA parameterized queries (no string concatenation)
- Specifications API for dynamic queries
- Spring Data JPA handles all SQL

### Input Validation

✅ **Implemented**:
- DTO-level validation (Jakarta Bean Validation)
- @NotNull, @NotBlank, @Size constraints
- Custom validators for business rules
- Global exception handler returns 400 for validation errors

---

## CI/CD Pipeline

**GitHub Actions** (.github/workflows/):
- Automated testing on push
- SpotBugs & Checkstyle checks
- Build Docker images
- Push to registry
- Deploy to staging/production

**Local Pre-commit**:
```bash
# Add pre-commit hook
npm install husky --save-dev
npx husky install
# Or manually: mvn clean verify before pushing
```

---

## Performance Optimization

### Database

- **Indexes**: On `tenant_id`, `id`, frequently queried fields
- **Pagination**: All list endpoints support pagination
- **Connection Pooling**: HikariCP configured in application.yml

### Caching

- **Spring Cache**: Can be enabled with @Cacheable annotations
- **Kafka**: Event batching for throughput

### Monitoring

- **JaCoCo**: Identify untested code paths
- **Spring Actuator**: Monitor request metrics
- **Logs**: Debug slow queries with Hibernate statistics

---

## Future Enhancements

1. **API Gateway**: Kong or Spring Cloud Gateway for cross-cutting concerns
2. **Service Mesh**: Istio for traffic management
3. **Distributed Tracing**: Sleuth + Zipkin integration
4. **API Versioning**: Header-based or URL path versioning
5. **GraphQL**: Alternative to REST API
6. **Caching Layer**: Redis for distributed caching
7. **Search Engine**: Elasticsearch for full-text search
8. **Rate Limiting**: Per-tenant API rate limits
9. **OAuth2/OIDC**: External identity provider integration
10. **Message Queue**: RabbitMQ alternative to Kafka

---

## Useful References

- **[README.md](README.md)** - Quick start guide
- **[API_CONTRACT.md](API_CONTRACT.md)** - Complete API specification
- [Spring Boot 3.2 Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/projects/spring-data-jpa)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [MapStruct User Guide](https://mapstruct.org/)
- [Flyway Documentation](https://flywaydb.org/documentation/)

---

**Last Updated**: 2024-01-15  
**Version**: 0.0.1-SNAPSHOT
