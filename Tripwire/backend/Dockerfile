# Stage 1: Build with Gradle
FROM gradle:8.9-jdk21 AS build
WORKDIR /app
COPY backend/ .      
RUN gradle bootJar --no-daemon

# Stage 2: Run with JDK
FROM openjdk:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
