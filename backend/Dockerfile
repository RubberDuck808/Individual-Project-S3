# Backend Dockerfile (PRODUCTION)
# Stage 1: JAR build is done in CI, so this image is runtime-only

FROM eclipse-temurin:21-jre
WORKDIR /app

# CI copies the built JAR artifact into this directory
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
