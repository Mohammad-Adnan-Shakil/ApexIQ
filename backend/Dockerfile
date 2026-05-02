# Multi-stage build for DeltaBox Spring Boot backend
# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first (for layer caching)
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Runtime with JRE
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR from builder stage and rename to app.jar
COPY --from=builder /build/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check (optional, Render will use this)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Build bust: 2026-05-02-09-44
