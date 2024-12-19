FROM maven:3.9.4-eclipse-temurin-17 AS builder
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
