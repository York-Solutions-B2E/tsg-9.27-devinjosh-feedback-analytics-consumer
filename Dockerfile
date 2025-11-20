## Build stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml pom.xml
COPY src src
RUN chmod +x mvnw 
RUN ./mvnw -q -DskipTests package

## Runtime stage
FROM eclipse-temurin:21-jre

# Curl so compose can run healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/target/feedback-analytics-consumer-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]

