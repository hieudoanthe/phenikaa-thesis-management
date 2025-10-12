# Build stage
FROM eclipse-temurin:17-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY discovery/discovery-server/pom.xml discovery/discovery-server/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build only discovery-server
RUN mvn clean package -pl discovery/discovery-server -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy JAR file
COPY --from=builder /app/discovery/discovery-server/target/*.jar discovery-server.jar

# Expose port
EXPOSE 8761

CMD ["java", "-jar", "discovery-server.jar"]