# =====================
# STAGE 1 — Build stage
# =====================

FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# 1. copy pom.xml separately to leverage Maven cache
COPY pom.xml .

# download dependencies for caching
RUN mvn -q dependency:go-offline

# 2. now copy the source code
COPY src ./src

# 3. build the JAR
RUN mvn -q clean package -DskipTests


# ===============================
# STAGE 2 — Final runtime image
# ===============================

FROM eclipse-temurin:21-jre

WORKDIR /app

# copy the built JAR from the build stage
COPY --from=build /app/target/product-service-0.0.1-SNAPSHOT.jar app.jar

# expose application port
EXPOSE 8080

# run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
