#FROM bellsoft/liberica-openjdk-alpine:21
#
#WORKDIR /app
#
#COPY mvnw .
#COPY .mvn/ .mvn
#COPY pom.xml .
#COPY src/ ./src
#
#RUN chmod +x mvnw
#RUN ./mvnw clean install -DskipTests
#
#EXPOSE 8080
#
#CMD ["java", "-jar", "target/Customer_Portal_29-0.0.1-SNAPSHOT.jar"]

FROM bellsoft/liberica-openjdk-alpine:21

WORKDIR /app

COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .
COPY src/ ./src

RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

# Force the database connection - these WILL be used
ENV SPRING_DATASOURCE_URL="jdbc:postgresql://neondb_owner:npg_omyzDB20MCpk@ep-muddy-night-aohjrf7n.c-2.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require"
ENV SPRING_DATASOURCE_USERNAME="neondb_owner"
ENV SPRING_DATASOURCE_PASSWORD="npg_omyzDB20MCpk"
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME="org.postgresql.Driver"

EXPOSE 8080

CMD ["java", "-jar", "target/Customer_Portal_29-0.0.1-SNAPSHOT.jar"]