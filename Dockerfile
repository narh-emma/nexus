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

# ---- Python + edge-tts CLI (needed by EdgeTtsService, which shells out to it) ----
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip \
    && pip3 install --no-cache-dir edge-tts \
    && apt-get purge -y --auto-remove python3-pip \
    && rm -rf /var/lib/apt/lists/*

RUN edge-tts --list-voices > /dev/null

RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=builder /workspace/target/nexus-backend-*.jar /app/app.jar

RUN mkdir -p /app/uploads/audio
RUN chown -R spring:spring /app

USER spring:spring
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]