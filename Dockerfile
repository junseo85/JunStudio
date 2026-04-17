# ==========================================
# STAGE 1: Build the Java JAR
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the pom.xml and download dependencies first (caches them for faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy your actual source code and compile it
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: The Production Image
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy ONLY the compiled jar file from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]