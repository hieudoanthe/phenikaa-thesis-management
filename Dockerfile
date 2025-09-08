# Multi-stage build for all services
FROM openjdk:21-jdk-slim as builder

# Set working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy entire source (simpler and avoids reactor errors)
COPY . .

# Build: install parent POM first to local repo, then build all modules
RUN mvn -q -N -f pom.xml install \
 && mvn -q clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

# Install necessary packages
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy built jars
COPY --from=builder /app/discovery/discovery-server/target/*.jar discovery-server.jar
COPY --from=builder /app/config/config-server/target/*.jar config-server.jar
COPY --from=builder /app/gateway/api-gateway/target/*.jar api-gateway.jar
COPY --from=builder /app/services/auth-service/target/*.jar auth-service.jar
COPY --from=builder /app/services/user-service/target/*.jar user-service.jar
COPY --from=builder /app/services/thesis-service/target/*.jar thesis-service.jar
COPY --from=builder /app/services/profile-service/target/*.jar profile-service.jar
COPY --from=builder /app/services/group-service/target/*.jar group-service.jar
COPY --from=builder /app/services/assign-service/target/*.jar assign-service.jar
COPY --from=builder /app/services/submission-service/target/*.jar submission-service.jar
COPY --from=builder /app/services/academic-config-service/target/*.jar academic-config-service.jar
COPY --from=builder /app/services/communication-log-service/target/*.jar communication-log-service.jar
COPY --from=builder /app/services/eval-service/target/*.jar eval-service.jar

# Expose ports
EXPOSE 8761 8888 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Default command (can be overridden)
CMD ["java", "-jar", "discovery-server.jar"]
