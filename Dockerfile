# Multi-stage build để giảm kích thước image
FROM eclipse-temurin:17-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy source code
COPY . .

# Build tất cả modules
RUN mvn clean package -DskipTests

# Runtime stage - chỉ copy JAR files cần thiết
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy JAR files
COPY --from=builder /app/discovery/discovery-server/target/*.jar discovery-server.jar
COPY --from=builder /app/config/config-server/target/*.jar config-server.jar
COPY --from=builder /app/gateway/api-gateway/target/*.jar api-gateway.jar
COPY --from=builder /app/services/user-service/target/*.jar user-service.jar
COPY --from=builder /app/services/auth-service/target/*.jar auth-service.jar
COPY --from=builder /app/services/thesis-service/target/*.jar thesis-service.jar
COPY --from=builder /app/services/profile-service/target/*.jar profile-service.jar
COPY --from=builder /app/services/submission-service/target/*.jar submission-service.jar
COPY --from=builder /app/services/communication-log-service/target/*.jar communication-log-service.jar
COPY --from=builder /app/services/eval-service/target/*.jar eval-service.jar
COPY --from=builder /app/services/assign-service/target/*.jar assign-service.jar
COPY --from=builder /app/services/academic-config-service/target/*.jar academic-config-service.jar

# Expose ports
EXPOSE 8761 8888 8080 8090 8081 8082 8083 8084 8085 8086 8087 8088

# Default command (can be overridden)
CMD ["java", "-jar", "discovery-server.jar"]