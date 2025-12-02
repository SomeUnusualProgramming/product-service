# Product Service

A simple Spring Boot project with PostgreSQL and Kafka.

## Requirements
- Java 17+
- Docker
- Maven

## Run with Docker Compose
```bash
docker-compose up --build
```
This will start:
PostgreSQL on port 5432
Kafka on port 9092
Zookeeper on port 2181
Spring Boot app on port 8080

Application Properties Configured via environment variables in Docker Compose:
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_KAFKA_BOOTSTRAP_SERVERS

Endpoints
GET /products – list all products
GET /products/{id} – get product by ID
POST /products – create product
DELETE /products/{id} – delete product
