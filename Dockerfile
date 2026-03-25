FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ ./src

RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

#EXPOSE 8080

CMD ["java", "-jar", "target/Customer_Portal_29-0.0.1-SNAPSHOT.jar"]