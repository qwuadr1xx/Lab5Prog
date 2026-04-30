FROM gradle:8-jdk17 AS builder
WORKDIR /workspace
COPY --chown=gradle:gradle . .
RUN gradle :server:fatJar :balancer:fatJar :app:fatJar --no-daemon --parallel

FROM eclipse-temurin:17-jre-alpine AS server
WORKDIR /app
COPY --from=builder /workspace/server/build/libs/server-1.0-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre-alpine AS balancer
WORKDIR /app
COPY --from=builder /workspace/balancer/build/libs/balancer-1.0-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:17-jre-alpine AS client
WORKDIR /app
COPY --from=builder /workspace/app/build/libs/app-1.0-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]