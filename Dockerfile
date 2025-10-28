# Étape 1 : Build avec Maven
FROM maven:3.8.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Étape 2 : Image légère pour exécuter le JAR
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/Digital_Logistics-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]