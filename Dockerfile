FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests clean package

FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

WORKDIR /app

COPY --from=build --chown=65532:65532 /workspace/target/*.jar app.jar

USER nonroot:nonroot

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
