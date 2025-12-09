# =====================
# STAGE 1 — Build stage
# =====================

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Copy parent pom.xml
COPY pom.xml .

# Copy product-service pom.xml (needed for monorepo build)
COPY product-service/pom.xml ./product-service/

# Copy integration-service pom.xml
COPY integration-service/pom.xml ./integration-service/

# Download dependencies for caching
RUN mvn -q dependency:go-offline -pl integration-service

# Copy source code
COPY integration-service/src ./integration-service/src

# Build integration-service JAR
RUN mvn -q clean package -DskipTests -pl integration-service -am


# ===============================
# STAGE 2 — Final runtime image
# ===============================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/integration-service/target/integration-service-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
