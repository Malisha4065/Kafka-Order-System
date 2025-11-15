# Stage 1: Build the application with Maven
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy the shared Avro schema
COPY src/main/avro /app/src/main/avro

# Copy the producer's pom.xml and maven wrapper
COPY order-producer/mvnw /app/mvnw
COPY order-producer/.mvn /app/.mvn
COPY order-producer/pom.xml /app/pom.xml

# Download dependencies & generate Avro sources
RUN ./mvnw dependency:go-offline
RUN ./mvnw generate-sources

# Copy the producer's source code
COPY order-producer/src /app/src

# Build the producer
RUN ./mvnw package -DskipTests

# Stage 2: Create the image with only the JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar /app/application.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/application.jar"]