# Stage 1: Build the application with Maven
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy the shared Avro schema
COPY src/main/avro /app/src/main/avro

# Copy the consumer's pom.xml and maven wrapper
COPY order-consumer/mvnw /app/mvnw
COPY order-consumer/.mvn /app/.mvn
COPY order-consumer/pom.xml /app/pom.xml

# Download dependencies & generate Avro sources
RUN ./mvnw dependency:go-offline
RUN ./mvnw generate-sources

# Copy the consumer's source code
COPY order-consumer/src /app/src

# Build the consumer
RUN ./mvnw package -DskipTests

# Stage 2: Create the image with only the JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar /app/application.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/application.jar"]