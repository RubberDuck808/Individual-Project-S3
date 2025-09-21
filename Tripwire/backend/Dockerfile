# Stage 1: Build with Gradle
FROM gradle:8.9-jdk21 AS build
WORKDIR /app
COPY backend/backend/ .
RUN gradle bootJar --no-daemon

# Stage 2: Run with JDK
FROM openjdk:21
WORKDIR /app
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
