# Use OpenJDK 25 as base image
FROM openjdk:25

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ ./src

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the Spring Boot app
RUN ./mvnw clean install -DskipTests

# Expose the port your app runs on
EXPOSE 8080

# Command to run the app
CMD ["java", "-jar", "target/Customer_Portal_29-0.0.1-SNAPSHOT.jar"]