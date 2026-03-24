# Use Java 21 LTS base image (works on Render)
FROM bellsoft/liberica-openjdk-alpine:21

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ ./src

# Give executable permission to Maven wrapper
RUN chmod +x mvnw

# Build the project (skip tests)
RUN ./mvnw clean install -DskipTests

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Start the Spring Boot app
CMD ["java", "-jar", "target/Customer_Portal_29-0.0.1-SNAPSHOT.jar"]