# Use OpenJDK 17 LTS as base image
FROM openjdk:17

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ ./src

# Make mvnw executable and build
RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

# Expose Spring Boot default port
EXPOSE 8080

# Start the Spring Boot app
CMD ["java", "-jar", "target/Customer_Portal_29-0.0.1-SNAPSHOT.jar"]