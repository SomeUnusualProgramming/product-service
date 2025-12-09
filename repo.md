# Product Service Repository

## Project Overview

**Product Service Monorepo** is a Spring Boot 3.2.5 microservices architecture featuring:
- **Product Service**: Core product management with multi-tenancy and event auditing
- **Integration Service**: Event consumer for downstream processing

The architecture implements **multi-tenant isolation**, **event-driven workflows** via Kafka, and **clean code principles** through DTO-based APIs and standardized exception handling.

**Tech Stack:**
- **Language**: Java 21
- **Framework**: Spring Boot 3.2.5 + Spring Data JPA + Spring Kafka
- **Build Tool**: Maven 3.6+
- **Database**: PostgreSQL 15
- **Message Broker**: Apache Kafka (wurstmeister)
- **ORM Mapping**: MapStruct for DTOs
- **Code Generation**: Lombok
- **Migrations**: Flyway
- **Quality Tools**: JaCoCo, SpotBugs, Checkstyle

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
.
├── product-service/                      # Product management microservice
│   ├── src/main/java/com/example/productservice/
│   │   ├── controller/                   # REST endpoints
│   │   │   ├── ProductController.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── service/                      # Business logic
│   │   │   └── ProductService.java
│   │   ├── repository/                   # Data access (JPA + Specifications)
│   │   │   ├── ProductRepository.java
│   │   │   ├── ProductSpecification.java
│   │   │   └── SortBuilder.java
│   │   ├── model/                        # JPA entities
│   │   │   ├── Product.java
│   │   │   ├── TenantEntity.java
│   │   │   └── HasTenantId.java
│   │   ├── dto/                          # Request/Response DTOs
│   │   │   ├── ProductRequestDTO.java
│   │   │   ├── ProductResponseDTO.java
│   │   │   ├── PageResponseDTO.java
│   │   │   └── ErrorResponse.java
│   │   ├── mapper/                       # MapStruct mappers
│   │   │   └── ProductMapper.java
│   │   ├── kafka/                        # Event streaming
│   │   │   ├── ProductProducer.java
│   │   │   ├── ProductConsumer.java
│   │   │   ├── KafkaConfig.java
│   │   │   └── HistoryBuilder.java
│   │   ├── exception/                    # Custom exceptions
│   │   │   ├── ProductNotFoundException.java
│   │   │   ├── ValidationException.java
│   │   │   ├── BusinessException.java
│   │   │   ├── ConflictException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── UnauthorizedException.java
│   │   │   └── TenantMissingException.java
│   │   ├── security/                     # Multi-tenancy context
│   │   │   ├── TenantContext.java
│   │   │   ├── TenantProvider.java
│   │   │   └── TenantValidator.java
│   │   ├── filter/                       # Servlet filters
│   │   │   └── TenantFilter.java
│   │   ├── listener/                     # Entity listeners
│   │   │   └── TenantEntityListener.java
│   │   ├── constant/                     # Constants
│   │   │   └── AppConstants.java
│   │   └── ProductServiceApplication.java
│   ├── src/test/java/                    # Unit & integration tests
│   ├── src/main/resources/
│   │   ├── application.yml               # Spring Boot config
│   │   ├── db/migration/                 # Flyway SQL migrations
│   │   └── logback-spring.xml            # Logging config
│   ├── pom.xml
│   └── Dockerfile
│
├── integration-service/                  # Event consumer service
│   ├── src/main/java/com/example/integrationservice/
│   │   ├── controller/
│   │   ├── kafka/                        # Kafka event consumers
│   │   └── IntegrationServiceApplication.java
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
│
├── k8s/                                  # Kubernetes manifests
│   ├── namespace.yaml
│   ├── postgres-statefulset.yaml
│   ├── kafka-statefulset.yaml
│   ├── zookeeper-deployment.yaml
│   ├── product-service-deployment.yaml
│   ├── integration-service-deployment.yaml
│   ├── configmap-secret.yaml
│   ├── ingress.yaml
│   ├── kustomization.yaml
│   ├── deploy.sh
│   └── delete.sh
│
├── docker-compose.yml                    # Docker Compose orchestration
├── pom.xml                               # Parent POM (modules)
├── .env                                  # Environment variables
├── init-databases.sql                    # DB initialization script
├── checkstyle.xml                        # Code style rules
├── spotbugs-exclude.xml                  # SpotBugs exclusions
├── API_CONTRACT.md                       # API specification
├── README.md                             # Quick start guide
└── repo.md                               # This file
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

### 1. Multi-Tenancy

Implements **row-level security** at the database layer:

```
Request → TenantFilter → TenantContext (thread-local)
                              ↓
                        All Repository queries
                        filtered by tenant_id
```

**Implementation:**
- `TenantFilter`: Servlet filter extracting tenant from HTTP headers (X-Tenant-ID)
- `TenantContext`: Thread-local storage for tenant ID in current request
- `TenantProvider`: Factory for accessing current tenant
- `TenantValidator`: Authorization checks before operations
- `TenantEntity`: Base class ensuring all entities have `tenant_id` field
- `TenantEntityListener`: JPA listener auto-populating tenant_id on persist

**Guarantees:**
- ✅ Data isolation: Queries automatically filtered
- ✅ No cross-tenant leakage possible
- ✅ Tenant context required for all operations
- ✅ Audit trail shows which tenant modified what

### 2. Event-Driven Architecture with Kafka

**Flow:**
```
Product CRUD → ProductProducer → Kafka topic (products-events)
                                      ↓
                           ProductConsumer (integration-service)
                           Stores in Integration DB
```

**Event Types:**
- `CREATED`: New product created
- `UPDATED`: Product modified
- `DELETED`: Product removed
- `LOW_STOCK`: Stock falls below threshold

**Implementation:**
- `ProductProducer.java`: Publishes to `products-events` topic
- `ProductConsumer.java`: Listens and processes events
- `HistoryBuilder.java`: Constructs event objects with metadata
- `KafkaConfig.java`: Topic configuration and consumer groups

**Audit Trail Access:**
```
GET /api/products/{id}/history → Returns all event versions with timestamps
```

### 3. Clean API with DTO-Based Separation

**Why DTOs?**
- Decouples API contract from database schema
- Enables backward compatibility during refactoring
- Centralized validation at API boundary
- Prevents accidental entity mutation exposure

**Flow:**
```
HTTP Request
    ↓
ProductRequestDTO (validation)
    ↓
ProductMapper (entity conversion)
    ↓
ProductService (business logic)
    ↓
Product (JPA entity)
    ↓
ProductMapper (back to DTO)
    ↓
ProductResponseDTO
    ↓
HTTP Response
```

**Key Classes:**
- `ProductRequestDTO`: Validates input (name, price, stock, etc.)
- `ProductResponseDTO`: Formats response with event metadata
- `ProductMapper`: Auto-generated MapStruct mapper
- `PageResponseDTO`: Wraps paginated results

### 4. Exception Handling & Standardization

**All errors return unified `ErrorResponse`:**

```json
{
  "errorId": "UUID-for-tracking",
  "status": "HTTP status code",
  "error": "ERROR_CODE",
  "message": "Human-readable description",
  "timestamp": "ISO-8601 timestamp",
  "path": "/api/products/...",
  "fieldErrors": {"field": "error message"},
  "traceId": "request-id-for-logging"
}
```

**Exception Hierarchy:**
```
AppException (base)
  ├── ProductNotFoundException (404)
  ├── ValidationException (400)
  ├── BusinessException (400)
  ├── ConflictException (409)
  ├── UnauthorizedException (401)
  └── TenantMissingException (403)
```

**Global Handler:** `GlobalExceptionHandler` uses Spring's `@RestControllerAdvice` to catch and format all exceptions consistently.

### 5. Data Access with Specifications

**Pattern:** Spring Data JPA with Specifications for dynamic queries

```
ProductRepository
  ├── extends JpaRepository
  └── extends JpaSpecificationExecutor
      └── ProductSpecification.java (dynamic predicates)
```

**Benefits:**
- Type-safe query building
- Reusable filter logic
- Pagination and sorting support
- Handles complex multi-tenant queries

### 6. Code Quality & Testing

**Tools:**
- **JaCoCo**: Code coverage reporting
- **SpotBugs**: Static bug detection (High confidence threshold)
- **Checkstyle**: Code style enforcement
- **Lombok**: Reduces boilerplate 95%

**Testing Coverage:**
- Unit tests for services and repositories
- Integration tests with embedded Kafka/H2
- Exception handling tests
- Multi-tenant isolation tests

---

## Build & Quality Tools

### Maven Plugin Configuration

```xml
<!-- Parent POM defines versions and common plugins -->
<properties>
  <java.version>21</java.version>
  <springdoc-openapi.version>2.0.0</springdoc-openapi.version>
  <jacoco-maven-plugin.version>0.8.11</jacoco-maven-plugin.version>
  <spotbugs-maven-plugin.version>4.8.3.1</spotbugs-maven-plugin.version>
  <maven-checkstyle-plugin.version>3.3.1</maven-checkstyle-plugin.version>
</properties>
```

**Key Plugins:**
- **maven-compiler-plugin**: Java 21 compilation with Lombok/MapStruct annotation processing
- **spring-boot-maven-plugin**: Builds executable JAR and enables hot reload
- **jacoco-maven-plugin**: Code coverage report generation (exec after tests)
- **spotbugs-maven-plugin**: Static analysis (effort=Max, threshold=High)
- **maven-checkstyle-plugin**: Code style verification against `checkstyle.xml`

### Quality Check Commands

```bash
# Run all tests with JaCoCo coverage
mvn clean test

# Run tests + SpotBugs + code coverage report
mvn clean verify

# Run only SpotBugs analysis
mvn spotbugs:check

# Run Checkstyle verification
mvn checkstyle:check

# Full build with all checks
mvn clean package

# Generate JaCoCo HTML report
mvn jacoco:report
# Report location: target/site/jacoco/index.html
```

### CI/CD Integration

Quality checks integrate with:
- **Pre-commit hooks**: Run format/lint
- **GitHub Actions** (`.github/workflows/`): Build, test, quality gates
- **Docker builds**: Multi-stage for optimized images

---

## Environment Configuration

### Docker Compose Setup

**File:** `.env` (root directory)
```bash
# Database
DB_USER=postgres
DB_PASSWORD=postgres
DB_PORT=5432

# Services
PRODUCT_SERVICE_PORT=8080
INTEGRATION_SERVICE_PORT=8081
```

**Service Environment Variables** (set in `docker-compose.yml`):
```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/products_db
  SPRING_DATASOURCE_USERNAME: ${DB_USER}
  SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Local Development (Standalone)

Create `local.env` or update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/products_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: product-service-group
  flyway:
    enabled: true
    baseline-on-migrate: true

logging:
  level:
    com.example.productservice: DEBUG
    org.springframework.web: INFO
```

### Kubernetes Deployment

**Files:** `k8s/*.yaml`
```bash
# Deploy to K8s
./k8s/deploy.sh

# View deployments
kubectl get deployments -n product-service

# Check pods
kubectl get pods -n product-service

# View logs
kubectl logs -f deployment/product-service -n product-service
```

---

## Database Schema

### Product Service Database

**Table: `products`**
```sql
CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  tenant_id VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  category VARCHAR(100) NOT NULL,
  price DECIMAL(12, 2) NOT NULL,
  stock_quantity INTEGER NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  event_time TIMESTAMP NOT NULL,
  original_product_id BIGINT,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  
  CONSTRAINT products_original_fk 
    FOREIGN KEY (original_product_id) 
    REFERENCES products(id)
);

CREATE INDEX idx_products_tenant_id ON products(tenant_id);
CREATE INDEX idx_products_original_id ON products(original_product_id);
```

**Columns:**
- `id`: Primary key (auto-generated)
- `tenant_id`: Multi-tenancy key (MANDATORY, every query filtered)
- `name`: Product name (max 255 chars)
- `description`: Product description (max 2000 chars)
- `category`: Product category (max 100 chars)
- `price`: Unit price (DECIMAL 12,2 = $999,999.99 max)
- `stock_quantity`: Available units (Integer)
- `event_type`: CREATED|UPDATED|DELETED|LOW_STOCK
- `event_time`: When event occurred (audit timestamp)
- `original_product_id`: Points to original version (null for first version)
- `created_at`: Record creation (JPA managed)
- `updated_at`: Last modification (JPA managed)

### Database Migrations

**Tool:** Flyway
**Location:** `product-service/src/main/resources/db/migration/`
**Pattern:** `V[version]__[description].sql`

Example:
```
V001__initial_schema.sql    - Products table creation
V002__add_indexes.sql       - Performance indexes
V003__add_constraints.sql   - Foreign key constraints
```

**Auto-execution:**
- Runs on application startup
- Version tracking in `flyway_schema_history` table
- Rollback prevention (no V000 versions)

### Integration Service Database

Separate schema for event consumption:
```sql
CREATE TABLE product_events (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  event_data JSONB,
  processed_at TIMESTAMP NOT NULL
);
```

---

## Key Features & Design Patterns

### Core Features
✅ **Product CRUD**: Create, Read, Update, Delete with full validation  
✅ **Multi-Tenancy**: Row-level isolation via tenant_id filtering  
✅ **Audit Trail**: All product changes recorded with event metadata  
✅ **Event Streaming**: Kafka integration for real-time event consumption  
✅ **API Versioning**: DTOs enable backward compatibility during schema changes  

### Code Quality
✅ **Exception Standardization**: Unified ErrorResponse format across all endpoints  
✅ **Input Validation**: Jakarta Bean Validation on DTOs  
✅ **Code Coverage**: JaCoCo reports and CI/CD gates  
✅ **Static Analysis**: SpotBugs high-confidence bug detection  
✅ **Style Enforcement**: Checkstyle linting via Maven  

### Testing Strategy
✅ **Unit Tests**: Service and repository logic isolation  
✅ **Integration Tests**: Embedded Kafka + H2 in-memory DB  
✅ **Exception Tests**: Verify error codes and messages  
✅ **Multi-Tenant Tests**: Ensure tenant isolation works correctly  
✅ **Kafka Tests**: Event producer/consumer verification  

### Deployment
✅ **Docker**: Multi-stage builds for optimized images  
✅ **Docker Compose**: Full stack (Kafka, PostgreSQL, services)  
✅ **Kubernetes**: YAML manifests with ConfigMaps/Secrets  
✅ **Health Checks**: Actuator endpoints for monitoring  
✅ **Graceful Shutdown**: Clean resource release on termination  

### Monitoring & Observability
✅ **Actuator**: `/actuator/health` and metrics endpoints  
✅ **Structured Logging**: Logback with JSON output capability  
✅ **Request Tracing**: Unique request IDs in error responses  
✅ **Performance Metrics**: JaCoCo coverage and SpotBugs reports  

---

## Development Workflow

### Prerequisites
1. Clone repository
2. Install Java 21, Maven 3.6+, Docker
3. Copy `.env.example` to `.env` (if needed)

### Development Loop

**1. Start services locally**
```bash
# Option A: Full Docker stack
docker-compose up -d

# Option B: Local development with external DB
# Start PostgreSQL and Kafka first, then:
mvn spring-boot:run
```

**2. Make your changes**

Follow these conventions:
- **Code Style**: Follow existing patterns (see `checkstyle.xml`)
- **No commented code**: Remove dead code
- **DTOs for APIs**: Always expose DTOs, never entities
- **Exception handling**: Use custom exceptions
- **Multi-tenancy**: Always consider tenant isolation
- **Kafka events**: Emit events for important state changes
- **Tests**: Write tests alongside code

Example structure:
```
feature: Add product sorting
├── ProductSpecification.java (add sort criterion)
├── SortBuilder.java (update if needed)
├── ProductService.java (update logic)
├── ProductRequestDTO.java (add sortBy field)
├── ProductControllerIntegrationTest.java (test scenario)
└── ProductServiceTest.java (test business logic)
```

**3. Run tests locally**
```bash
# Unit tests only
mvn test

# Full verification (tests + quality checks)
mvn clean verify

# Specific test class
mvn test -Dtest=ProductServiceTest

# Generate coverage report
mvn jacoco:report
# Open: target/site/jacoco/index.html
```

**4. Commit & push**
```bash
git checkout -b feature/your-feature
git add .
git commit -m "feat: add product sorting"
git push origin feature/your-feature
```

**5. Create pull request**

PR template (if available):
```markdown
## Changes
- Brief description of what changed

## Type
- [ ] Feature
- [ ] Bug fix
- [ ] Refactoring
- [ ] Documentation

## Testing
- [ ] Tests added/updated
- [ ] mvn clean verify passes
- [ ] Manual testing done

## Multi-tenancy Check
- [ ] Tenant context considered
- [ ] No cross-tenant data leakage

## Breaking Changes
- [ ] No breaking API changes
- [ ] DTOs updated if needed
```

**6. Code review & merge**
- All CI checks must pass
- Code coverage acceptable
- Approval from reviewers

---

## Useful Commands Reference

### Maven Build
```bash
# Clean and package (build JAR)
mvn clean package

# Skip tests during build (only when necessary!)
mvn clean package -DskipTests

# Run only tests with coverage
mvn clean test

# Full verification with all checks
mvn clean verify

# Rebuild without cleaning
mvn package
```

### Maven Quality Checks
```bash
# Run SpotBugs analysis
mvn spotbugs:check

# Run SpotBugs GUI
mvn spotbugs:gui

# Run Checkstyle verification
mvn checkstyle:check

# Generate JaCoCo coverage report
mvn jacoco:report
# Report location: target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run test method
mvn test -Dtest=ProductServiceTest#testCreateProduct
```

### Running Applications
```bash
# Run Product Service locally (requires Kafka + PostgreSQL)
mvn spring-boot:run -pl product-service

# Run Integration Service locally
mvn spring-boot:run -pl integration-service

# Run with custom profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Docker & Docker Compose
```bash
# Start all services
docker-compose up -d

# Start and rebuild images
docker-compose up --build

# View logs (all services)
docker-compose logs -f

# View specific service logs
docker-compose logs -f product-service
docker-compose logs -f kafka
docker-compose logs -f db

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Health check
docker-compose ps
```

### Database & PostgreSQL
```bash
# Connect to PostgreSQL
docker exec -it postgres-db psql -U postgres -d products_db

# Useful SQL queries
# List all tables
\dt

# View product history for tenant
SELECT * FROM products WHERE tenant_id = 'tenant-001' ORDER BY event_time DESC;

# Count products by event type
SELECT event_type, COUNT(*) FROM products GROUP BY event_type;

# Exit psql
\q
```

### Kafka & Messaging
```bash
# View Kafka logs
docker-compose logs -f kafka

# View Zookeeper logs
docker-compose logs -f zookeeper

# Check if Kafka is healthy
docker exec kafka kafka-broker-api-versions.sh --bootstrap-server kafka:9092

# List Kafka topics
docker exec kafka kafka-topics.sh --list --bootstrap-server kafka:9092

# View messages from topic
docker exec kafka kafka-console-consumer.sh \
  --topic products-events \
  --from-beginning \
  --bootstrap-server kafka:9092
```

### Development Shortcuts
```bash
# Clean all compiled classes and test results
mvn clean

# Format code (if formatter configured)
mvn fmt:format

# Install without running tests
mvn install -DskipTests

# Skip javadoc generation
mvn clean package -Dspringboot.build.skipTests=true

# Rebuild dependency tree
mvn dependency:tree
```

---

## API Documentation

Complete API specifications: **[API_CONTRACT.md](API_CONTRACT.md)**

Includes:
- Endpoint documentation (GET/POST/PUT/DELETE)
- Request/response JSON examples
- Validation rules and constraints
- Error codes and scenarios
- Tenant context requirements
- Audit trail tracking details
- API stability & backward compatibility guarantees

### Quick API Reference

**Base Path:** `/api/products`

| Endpoint | Method | Status | Purpose |
|----------|--------|--------|---------|
| `/` | GET | 200 | List all products (paginated) |
| `/{id}` | GET | 200/404 | Get product by ID |
| `/{id}/history` | GET | 200/404 | Get product audit history |
| `/` | POST | 201/400 | Create product |
| `/{id}` | PUT | 200/400/404 | Update product |
| `/{id}` | DELETE | 204/404 | Delete product |

**Health Check:** `GET /actuator/health`

---

## Related Documentation

- **[API_CONTRACT.md](API_CONTRACT.md)**: Comprehensive API specification, validation rules, error scenarios
- **[README.md](README.md)**: Quick start guide with setup instructions
- **[docker-compose.yml](docker-compose.yml)**: Service orchestration and networking
- **[pom.xml](pom.xml)**: Maven dependencies and build configuration

---

## Troubleshooting

### Service Won't Start
1. Check port availability (8080 for product-service, 8081 for integration-service)
2. Verify PostgreSQL and Kafka are running: `docker-compose ps`
3. Check logs: `docker-compose logs -f product-service`

### Kafka Connection Issues
```bash
# Verify Kafka is healthy
docker exec kafka kafka-broker-api-versions.sh --bootstrap-server kafka:9092

# Check topics exist
docker exec kafka kafka-topics.sh --list --bootstrap-server kafka:9092
```

### Database Connection Errors
```bash
# Connect to PostgreSQL and verify databases
docker exec -it postgres-db psql -U postgres

# List databases
\l

# Connect to products_db
\c products_db

# List tables
\dt
```

### Test Failures
```bash
# Run with verbose output
mvn test -e -X

# Run specific failing test
mvn test -Dtest=ProductServiceTest#testMethod
```

---

## Version History

| Version | Date | Status | Key Changes |
|---------|------|--------|------------|
| 0.0.1-SNAPSHOT | Current | Active | Initial multi-tenant architecture with Kafka |

---

## Contributing Guidelines

1. **Code Quality First**: All changes must pass `mvn clean verify`
2. **Test Coverage**: Write tests for new features
3. **No Breaking Changes**: Use DTOs to ensure API stability
4. **Tenant Awareness**: Always consider multi-tenancy implications
5. **Documentation**: Update docs when APIs change

---

## License

Proprietary - Unauthorized copying prohibited

---

## Support & Contact

- **Issues**: GitHub Issues (if public) or internal ticket system
- **Documentation**: See markdown files in repository root
- **Architecture Questions**: See Architecture Highlights section above
