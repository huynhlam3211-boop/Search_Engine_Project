# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Giai doan 1 — BIEN DICH
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .

RUN mvn -B -q dependency:go-offline || true

COPY src ./src

RUN mvn -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# Giai doan 2 — CHAY
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN useradd --create-home --shell /bin/false vnsearch

COPY --from=build /build/target/*.jar app.jar

COPY data/seed-documents.json data/seed-documents.json
RUN chown -R vnsearch:vnsearch /app

USER vnsearch
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
