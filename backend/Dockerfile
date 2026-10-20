# Multi-stage Dockerfile for Spliz Backend & Embedded UI
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and backend pom.xml
COPY backend/mvnw backend/mvnw.cmd backend/pom.xml ./
COPY backend/.mvn ./.mvn
RUN chmod +x ./mvnw

# Copy backend source code
COPY backend/src ./src

# Automatically copy frontend assets into static web resources during Docker build
COPY frontend/ ./src/main/resources/static/

# Package production executable JAR
RUN ./mvnw clean package -DskipTests

# Production Runtime Image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/fairshare-backend-*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
