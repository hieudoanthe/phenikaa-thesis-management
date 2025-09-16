# Multi-stage build để giảm kích thước image
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app

# Copy chỉ pom.xml trước để cache dependencies
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY discovery/discovery-server/pom.xml discovery/discovery-server/
COPY config/config-server/pom.xml config/config-server/
COPY gateway/api-gateway/pom.xml gateway/api-gateway/
COPY services/user-service/pom.xml services/user-service/
COPY services/thesis-service/pom.xml services/thesis-service/
COPY services/submission-service/pom.xml services/submission-service/
COPY services/communication-log-service/pom.xml services/communication-log-service/
COPY services/eval-service/pom.xml services/eval-service/
COPY services/assign-service/pom.xml services/assign-service/
COPY services/academic-config-service/pom.xml services/academic-config-service/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

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
COPY --from=builder /app/services/thesis-service/target/*.jar thesis-service.jar
COPY --from=builder /app/services/submission-service/target/*.jar submission-service.jar
COPY --from=builder /app/services/communication-log-service/target/*.jar communication-log-service.jar
COPY --from=builder /app/services/eval-service/target/*.jar eval-service.jar
COPY --from=builder /app/services/assign-service/target/*.jar assign-service.jar
COPY --from=builder /app/services/academic-config-service/target/*.jar academic-config-service.jar

EXPOSE 8080
CMD ["java", "-jar", "discovery-server.jar"]
