# Stage 1: Build JAR with Gradle
FROM gradle:8.9-jdk21 AS build
WORKDIR /workspace

# Copy everything
COPY . .

# Build the Spring Boot jar (bootJar task)
RUN ./gradlew clean bootJar --no-daemon

# Stage 2: Run JAR
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install wget for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

# Copy the jar from the build stage
COPY --from=build /workspace/build/libs/*.jar app.jar

# Cloud Run requires this
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
