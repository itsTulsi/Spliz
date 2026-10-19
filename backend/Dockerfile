# Multi-stage Dockerfile for FairShare Intelligence Backend & Embedded UI
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and dependency definitions
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Copy project source code (includes frontend assets in src/main/resources/static)
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Production Runtime Image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/fairshare-backend-*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
