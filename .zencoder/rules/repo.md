---
description: Repository Information Overview
alwaysApply: true
---

# Product Service Monorepo Information

## Repository Summary

A Spring Boot 3.5.0 microservices monorepo featuring product management with multi-tenant support, event auditing, and Apache Kafka integration. The repository contains two independently deployable services (Product Service and Integration Service) orchestrated via Docker Compose with PostgreSQL and Kafka.

## Repository Structure

```
product-service/
├── product-service/              # Product microservice
│   ├── src/main/java/com/example/productservice/
│   ├── src/test/java/            # 8+ test classes
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/         # Flyway migrations
│   ├── Dockerfile                # Multi-stage build
│   └── pom.xml
├── integration-service/          # Integration microservice
│   ├── src/main/java/com/example/integrationservice/
│   ├── src/test/java/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   ├── Dockerfile                # Multi-stage build
│   └── pom.xml
├── k8s/                          # Kubernetes deployment configs
├── pom.xml                       # Parent POM (monorepo)
├── docker-compose.yml            # Service orchestration
├── .env                          # Environment variables
├── checkstyle.xml                # Code style rules
├── spotbugs-exclude.xml          # Bug detection exclusions
└── .mvn/wrapper/                 # Maven wrapper
```

### Main Components

- **Product Service**: REST API for product management with event publishing, port 8080
- **Integration Service**: Data integration service consuming product events, port 8081
- **PostgreSQL**: Separate databases for each service (products_db, integration_db), port 5432
- **Apache Kafka**: Event streaming broker with Zookeeper, port 9092
- **Ollama**: AI/ML service for integration tasks, port 11434 (GPU-enabled)

## Language & Runtime

**Language**: Java  
**Version**: 21 (enforced via `maven.compiler.source` and `maven.compiler.target`)  
**Framework**: Spring Boot 3.5.0  
**Build System**: Maven 3.9.11  
**Package Manager**: Maven Central Repository

## Dependencies

### Key Frameworks & Libraries

- **Spring Boot**: Web, Data JPA, Kafka, Validation, Actuator, WebFlux, Test
- **Data & Persistence**: PostgreSQL JDBC 42.7.7, Flyway 11.8.1 (database migrations)
- **Code Generation**: Lombok 1.18.42, MapStruct 1.5.5.Final
- **Serialization**: Jackson (databind, XML format, JSR310)
- **Utilities**: Commons CSV 1.10.0
- **Testing**: JUnit (via spring-boot-starter-test), H2 (in-memory DB for tests), Spring Kafka Test
- **Code Quality**: JaCoCo 0.8.14, SpotBugs 4.8.3.1, Checkstyle 3.3.1

### Dependency Management

Dependencies are centrally managed in the parent `pom.xml` with version properties. Both services inherit common dependencies while adding service-specific ones (e.g., WebFlux in integration-service).

## Build & Installation

### Build Commands

```bash
# Build all modules
mvn clean package

# Build specific module
mvn clean package -pl product-service -am

# Skip tests during build
mvn clean package -DskipTests
```

### Installation

```bash
# Using Docker Compose (recommended)
docker-compose up --build

# Local development (requires PostgreSQL and Kafka)
mvn spring-boot:run -pl product-service
mvn spring-boot:run -pl integration-service
```

### Entry Points

- **Product Service**: `com.example.productservice.ProductServiceApplication`
- **Integration Service**: `com.example.integrationservice.IntegrationServiceApplication`

## Docker

### Product Service Dockerfile

**Path**: `product-service/Dockerfile`  
**Base Image**: `eclipse-temurin:21-jre` (runtime), `maven:3.9.11-eclipse-temurin-21` (build)  
**Port**: 8080  
**Build Strategy**: Two-stage (Maven build → runtime)  
**Artifact**: `product-service-0.0.1-SNAPSHOT.jar`

### Integration Service Dockerfile

**Path**: `integration-service/Dockerfile`  
**Base Image**: `eclipse-temurin:21-jre` (runtime), `maven:3.9.11-eclipse-temurin-21` (build)  
**Port**: 8081  
**Build Strategy**: Two-stage (Maven build → runtime)  
**Artifact**: `integration-service-0.0.1-SNAPSHOT.jar`

### Docker Compose Services

- **db**: PostgreSQL 15 (port 5432, volumes: `postgres_data`)
- **zookeeper**: Wurstmeister Zookeeper (port 2181)
- **kafka**: Wurstmeister Kafka (port 9092)
- **product-service**: Custom build (port 8080)
- **integration-service**: Custom build (port 8081)
- **ollama**: Ollama AI/ML (port 11434, GPU-capable)

**Configuration**: Database credentials from `.env` (default: postgres/postgres), health checks on all services, service dependencies (Kafka depends on Zookeeper, services depend on DB and Kafka).

## Testing

### Testing Framework

**Framework**: JUnit (via `spring-boot-starter-test`) with Spring Boot Test support  
**Additional Tools**: Spring Kafka Test, H2 (in-memory database for integration tests)  
**Code Coverage**: JaCoCo 0.8.14 (target ≥80%)

### Test Locations & Patterns

- **Product Service**: `product-service/src/test/java/com/example/productservice/`
- **Test Classes**: `*Test.java` (e.g., `ProductServiceTest.java`, `ProductControllerIntegrationTest.java`)
- **Coverage Report**: Generated at `target/site/jacoco/index.html`

### Run Commands

```bash
# All tests with coverage
mvn clean test

# Specific test class
mvn test -Dtest=ProductServiceTest

# Full verification (tests + SpotBugs + coverage)
mvn verify

# SpotBugs analysis
mvn spotbugs:check

# Generate coverage report
mvn jacoco:report
```

## Configuration

### Application Properties

- **Database**: Spring Data JPA with PostgreSQL JDBC driver
- **Kafka**: Spring Kafka bootstrap servers (default: localhost:9092)
- **Logging**: Logback Spring XML configuration
- **Actuator**: Health checks on `/actuator/health`

### Configuration Files

- **Parent**: `pom.xml` (properties, dependency management, plugin management)
- **Product Service**: `product-service/src/main/resources/application.yml`
- **Integration Service**: `integration-service/src/main/resources/application.yml`
- **Database**: Flyway migrations in `src/main/resources/db/migration/`

### Quality & Standards

- **Checkstyle**: `checkstyle.xml` enforces code style (max line 120 chars, Javadoc on public methods, no trailing whitespace)
- **SpotBugs**: `spotbugs-exclude.xml` configures bug detection (max effort, high threshold)
- **Code Standards**: All public methods require Javadoc with `@param`, `@return`, `@throws`; parameters must be `final`
