# syntax=docker/dockerfile:1.6

##
## Build stage
##
FROM --platform=linux/amd64 maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

##
## Runtime stage
##
FROM --platform=linux/amd64 eclipse-temurin:17-jre-alpine
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""
WORKDIR /app

COPY --from=build /workspace/target/sql-generator-service-*.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

