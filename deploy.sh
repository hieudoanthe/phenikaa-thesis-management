#!/bin/bash

echo "Starting deployment of Phenikaa Thesis Management System (Monorepo)..."

# Kiểm tra Docker
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Tạo thư mục uploads nếu chưa có
mkdir -p uploads/traditional uploads/nio

# Dừng các container cũ
echo "Stopping existing containers..."
docker-compose down

# Xóa các image cũ để build lại
echo "Removing old images..."
docker system prune -f

# Build và start services với multi-stage build
echo "Building monorepo and starting services..."
echo "   - Building all modules with Maven..."
echo "   - Creating optimized runtime images..."

# Build tất cả services cùng lúc (monorepo advantage)
docker-compose build --no-cache --parallel

# Start services theo thứ tự dependency
echo "Starting infrastructure services first..."
docker-compose up -d redis discovery-server

echo "Waiting for infrastructure to be ready..."
sleep 30

echo "Starting config server..."
docker-compose up -d config-server

echo "Waiting for config server..."
sleep 20

echo "Starting all microservices..."
docker-compose up -d api-gateway auth-service user-service thesis-service profile-service eval-service assign-service submission-service academic-config-service communication-log-service

# Kiểm tra trạng thái
echo "Waiting for all services to start..."
sleep 60

# Kiểm tra health
echo "Checking service health..."
docker-compose ps

# Function để hiển thị logs với màu
check_service_logs() {
    local service_name=$1
    local tail_lines=${2:-10}

    echo -e "\033[1;34m=== $service_name ===\033[0m"
    docker-compose logs "$service_name" | tail -"$tail_lines"
    echo ""
}

# Kiểm tra logs của tất cả các service
echo "Checking service logs..."

# Infrastructure services
check_service_logs "redis" 5
check_service_logs "discovery-server" 10
check_service_logs "config-server" 10
check_service_logs "api-gateway" 10

# Microservices
check_service_logs "auth-service" 10
check_service_logs "user-service" 10
check_service_logs "thesis-service" 10
check_service_logs "profile-service" 10
check_service_logs "eval-service" 10
check_service_logs "assign-service" 10
check_service_logs "submission-service" 10
check_service_logs "academic-config-service" 10
check_service_logs "communication-log-service" 10

# Kiểm tra health status
echo "Checking service health status..."

# Danh sách các service có health check
health_services=(
    "api-gateway:8080"
    "auth-service:8090"
    "user-service:8081"
    "thesis-service:8082"
    "profile-service:8083"
    "eval-service:8084"
    "assign-service:8085"
    "submission-service:8086"
    "academic-config-service:8087"
    "communication-log-service:8088"
)

for service_port in "${health_services[@]}"; do
    service_name=$(echo "$service_port" | cut -d: -f1)
    port=$(echo "$service_port" | cut -d: -f2)

    echo -n "Health check $service_name: "
    
    # Thử /actuator/health trước
    health_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:"$port"/actuator/health 2>/dev/null)
    
    # Nếu 401, thử root endpoint (thường không cần auth)
    if [ "$health_status" = "401" ]; then
        health_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:"$port"/ 2>/dev/null)
    fi
    
    # Nếu vẫn 401, coi như service đang chạy nhưng cần auth
    if [ "$health_status" = "401" ]; then
        echo -e "\033[1;33m UP (Auth Required)\033[0m"
    elif [ "$health_status" = "200" ]; then
        echo -e "\033[1;32m UP\033[0m"
    else
        echo -e "\033[1;31m DOWN ($health_status)\033[0m"
    fi
done

# Kiểm tra Redis
echo -n "Redis status: "
redis_status=$(docker exec redis redis-cli ping 2>/dev/null)
if [ "$redis_status" = "PONG" ]; then
    echo -e "\033[1;32m UP\033[0m"
else
    echo -e "\033[1;31m DOWN\033[0m"
fi

# Kiểm tra Eureka
echo -n "Eureka status: "
eureka_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8761 2>/dev/null)
if [ "$eureka_status" = "200" ]; then
    echo -e "\033[1;32m UP\033[0m"
else
    echo -e "\033[1;31m DOWN ($eureka_status)\033[0m"
fi

# Kiểm tra Config Server
echo -n "Config Server status: "
config_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8888/actuator/health 2>/dev/null)
if [ "$config_status" = "200" ]; then
    echo -e "\033[1;32m UP\033[0m"
else
    echo -e "\033[1;31m DOWN ($config_status)\033[0m"
fi

echo "Monorepo deployment completed!"
echo "Services available at:"
echo "   - API Gateway: https://$(curl -s ifconfig.me):8080"
echo "   - Auth Service: https://$(curl -s ifconfig.me):8090"
echo "   - User Service: https://$(curl -s ifconfig.me):8081"
echo "   - Thesis Service: https://$(curl -s ifconfig.me):8082"
echo "   - Profile Service: https://$(curl -s ifconfig.me):8083"
echo "   - Eval Service: https://$(curl -s ifconfig.me):8084"
echo "   - Assign Service: https://$(curl -s ifconfig.me):8085"
echo "   - Submission Service: https://$(curl -s ifconfig.me):8086"
echo "   - Academic Config Service: https://$(curl -s ifconfig.me):8087"
echo "   - Communication Log Service: https://$(curl -s ifconfig.me):8088"
echo "   - Eureka Dashboard: https://$(curl -s ifconfig.me):8761"
echo "   - Config Server: https://$(curl -s ifconfig.me):8888"
echo "   - Redis: $(curl -s ifconfig.me):6379"
echo ""
echo "Monorepo Services Status:"
echo "   - Discovery Server (Eureka): Port 8761"
echo "   - Config Server: Port 8888"
echo "   - API Gateway: Port 8080"
echo "   - Auth Service: Port 8090"
echo "   - User Service: Port 8081"
echo "   - Thesis Service: Port 8082"
echo "   - Profile Service: Port 8083"
echo "   - Eval Service: Port 8084"
echo "   - Assign Service: Port 8085"
echo "   - Submission Service: Port 8086"
echo "   - Academic Config Service: Port 8087"
echo "   - Communication Log Service: Port 8088"
echo "   - Redis: Port 6379"
echo ""
echo "Monorepo Advantages:"
echo "   - Single Maven build for all services"
echo "   - Shared common-lib dependency"
echo "   - Optimized Docker multi-stage build"
echo "   - Centralized dependency management"