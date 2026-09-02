# syntax=docker/dockerfile:1

# Build stage — must match java.version in pom.xml (25)
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw \
    && ./mvnw -B -q dependency:resolve dependency:resolve-plugins -DskipTests

COPY src src
RUN ./mvnw -B -q -DskipTests package \
    && JAR="$(ls target/aura_fresh_backend-*.jar | grep -v '\.original$' | head -1)" \
    && cp "$JAR" /app/app.jar

# Runtime stage
FROM eclipse-temurin:25-jre-jammy AS runtime
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring \
    && apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build --chown=spring:spring /app/app.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
