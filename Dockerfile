FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

COPY pom.xml .

COPY task-tracker-backend/pom.xml task-tracker-backend/pom.xml
COPY task-tracker-email-sender/pom.xml task-tracker-email-sender/pom.xml
COPY task-tracker-scheduler/pom.xml task-tracker-scheduler/pom.xml

RUN mvn -pl task-tracker-backend dependency:go-offline -B

COPY task-tracker-backend/src ./task-tracker-backend/src

RUN mvn clean package -pl task-tracker-backend -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/task-tracker-backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]