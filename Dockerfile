# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package

FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

WORKDIR /app

LABEL org.opencontainers.image.title="Tech Challenge Oficina - Fase 2"
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:InitialRAMPercentage=25"

COPY --from=build --chown=65532:65532 /workspace/target/*.jar app.jar

USER nonroot:nonroot

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
