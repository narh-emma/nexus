# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=builder /workspace/target/nexus-backend-*.jar /app/app.jar
RUN chown -R spring:spring /app

USER spring:spring
EXPOSE 8081
ENV SPRING_PROFILES_ACTIVE=dev
ENV PORT=8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
