# Stage 1: build
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/target/profile-service-*.jar app.jar
EXPOSE 8083
CMD ["java",]()
