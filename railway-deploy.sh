#!/bin/bash

# Railway deployment script for Phenikaa Thesis Management
# This script helps deploy individual services to Railway

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Railway Deployment Script for Phenikaa Thesis Management${NC}"
echo "=================================================="

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo -e "${RED}❌ Railway CLI is not installed. Please install it first:${NC}"
    echo "npm install -g @railway/cli"
    echo "railway login"
    exit 1
fi

# Check if user is logged in
if ! railway whoami &> /dev/null; then
    echo -e "${RED}❌ Please login to Railway first:${NC}"
    echo "railway login"
    exit 1
fi

# Function to deploy a service
deploy_service() {
    local service_name=$1
    local jar_file=$2
    local port=$3
    
    echo -e "${YELLOW}📦 Deploying $service_name...${NC}"
    
    # Create a temporary directory for the service
    local temp_dir="temp_$service_name"
    mkdir -p "$temp_dir"
    
    # Copy the specific JAR file
    cp "$jar_file" "$temp_dir/"
    
    # Create a simple Dockerfile for the service
    cat > "$temp_dir/Dockerfile" << EOF
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file
COPY *.jar app.jar

# Expose port
EXPOSE $port

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
  CMD curl -f http://localhost:$port/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
EOF

    # Deploy to Railway
    cd "$temp_dir"
    railway up --service "$service_name" --detach
    
    # Clean up
    cd ..
    rm -rf "$temp_dir"
    
    echo -e "${GREEN}✅ $service_name deployed successfully!${NC}"
}

# Build the project first
echo -e "${YELLOW}🔨 Building the project...${NC}"
mvn clean package -DskipTests

# Deploy services in order
echo -e "${YELLOW}🚀 Starting deployment...${NC}"

# 1. Discovery Server (must be first)
deploy_service "discovery-server" "discovery/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar" "8761"

# 2. Config Server
deploy_service "config-server" "config/config-server/target/config-server-1.0-SNAPSHOT.jar" "8888"

# 3. API Gateway
deploy_service "api-gateway" "gateway/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar" "8080"

# 4. Auth Service
deploy_service "auth-service" "services/auth-service/target/auth-service-1.0-SNAPSHOT.jar" "8090"

# 5. User Service
deploy_service "user-service" "services/user-service/target/user-service-1.0-SNAPSHOT.jar" "8081"

# 6. Thesis Service
deploy_service "thesis-service" "services/thesis-service/target/thesis-service-1.0-SNAPSHOT.jar" "8082"

# 7. Profile Service
deploy_service "profile-service" "services/profile-service/target/profile-service-1.0-SNAPSHOT.jar" "8083"

# 8. Group Service
deploy_service "group-service" "services/group-service/target/group-service-1.0-SNAPSHOT.jar" "8084"

# 9. Assign Service
deploy_service "assign-service" "services/assign-service/target/assign-service-1.0-SNAPSHOT.jar" "8085"

# 10. Submission Service
deploy_service "submission-service" "services/submission-service/target/submission-service-1.0-SNAPSHOT.jar" "8086"

# 11. Academic Config Service
deploy_service "academic-config-service" "services/academic-config-service/target/academic-config-service-1.0-SNAPSHOT.jar" "8087"

# 12. Communication Log Service
deploy_service "communication-log-service" "services/communication-log-service/target/communication-log-service-1.0-SNAPSHOT.jar" "8088"

# 13. Eval Service
deploy_service "eval-service" "services/eval-service/target/eval-service-1.0-SNAPSHOT.jar" "8089"

echo -e "${GREEN}🎉 All services deployed successfully!${NC}"
echo -e "${YELLOW}📝 Next steps:${NC}"
echo "1. Set up PostgreSQL database on Railway"
echo "2. Set up MongoDB database on Railway" 
echo "3. Set up Redis database on Railway"
echo "4. Configure environment variables for each service"
echo "5. Update service URLs in configuration files"
