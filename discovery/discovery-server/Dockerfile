# Build stage
FROM openjdk:17-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY discovery/discovery-server/pom.xml discovery/discovery-server/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build only discovery-server
RUN mvn clean package -pl discovery/discovery-server -am -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=builder /app/discovery/discovery-server/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
