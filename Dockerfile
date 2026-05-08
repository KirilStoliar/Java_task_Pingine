FROM maven:3.8.4-openjdk-11-slim

WORKDIR /app

COPY target/fleet-pulse-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]