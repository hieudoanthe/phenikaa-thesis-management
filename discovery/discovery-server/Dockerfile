# Build stage
FROM eclipse-temurin:17-jdk as builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

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
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/discovery/discovery-server/target/*.jar app.jar

EXPOSE 8761
CMD ["java", "-jar", "discovery-server.jar"]
