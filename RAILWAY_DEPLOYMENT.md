# Railway Deployment Guide - Phenikaa Thesis Management

## Tổng quan

Dự án Phenikaa Thesis Management là một hệ thống microservices gồm 13 services. Do Railway có giới hạn về số lượng services miễn phí, chúng ta có 2 phương án:

### Phương án 1: Deploy từng service riêng biệt (Khuyến nghị cho production)

### Phương án 2: Deploy tất cả services trong một container (Phù hợp cho development/testing)

## Phương án 1: Deploy từng service riêng biệt

### Bước 1: Cài đặt Railway CLI

```bash
# Cài đặt Railway CLI
npm install -g @railway/cli

# Đăng nhập vào Railway
railway login
```

### Bước 2: Tạo project trên Railway

```bash
# Tạo project mới
railway init

# Hoặc kết nối với project hiện có
railway link
```

### Bước 3: Tạo databases

Trên Railway Dashboard, tạo các databases sau:

1. **PostgreSQL Database** (cho các services chính)
2. **MongoDB Database** (cho communication-log-service)
3. **Redis Database** (cho caching)

### Bước 4: Deploy từng service

```bash
# Build project
mvn clean package -DskipTests

# Deploy discovery-server (phải deploy đầu tiên)
railway up --service discovery-server

# Deploy config-server
railway up --service config-server

# Deploy các services khác...
```

### Bước 5: Cấu hình Environment Variables

Cho mỗi service, cần cấu hình các biến môi trường:

#### Discovery Server

```
SPRING_PROFILES_ACTIVE=prod
PORT=8761
```

#### Config Server

```
SPRING_PROFILES_ACTIVE=prod
PORT=8888
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://discovery-server-production.up.railway.app/eureka/
```

#### API Gateway

```
SPRING_PROFILES_ACTIVE=prod
PORT=8080
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://discovery-server-production.up.railway.app/eureka/
```

#### Các Microservices (auth, user, thesis, etc.)

```
SPRING_PROFILES_ACTIVE=prod
PORT=<service-port>
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=https://discovery-server-production.up.railway.app/eureka/
SPRING_CLOUD_CONFIG_URI=https://config-server-production.up.railway.app
DATABASE_URL=${{RAILWAY_DATABASE_URL}}
MONGODB_URI=${{RAILWAY_MONGODB_URI}}
REDIS_HOST=${{RAILWAY_REDIS_HOST}}
REDIS_PORT=${{RAILWAY_REDIS_PORT}}
REDIS_PASSWORD=${{RAILWAY_REDIS_PASSWORD}}
```

## Phương án 2: Deploy tất cả trong một container

### Bước 1: Tạo Dockerfile cho monolith

```dockerfile
FROM openjdk:21-jdk-slim as builder

WORKDIR /app
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY . .
RUN mvn -q -N -f pom.xml install && mvn -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy all JAR files
COPY --from=builder /app/discovery/discovery-server/target/*.jar discovery-server.jar
COPY --from=builder /app/config/config-server/target/*.jar config-server.jar
COPY --from=builder /app/gateway/api-gateway/target/*.jar api-gateway.jar
COPY --from=builder /app/services/*/target/*.jar ./

# Create startup script
RUN echo '#!/bin/bash\n\
java -jar discovery-server.jar &\n\
sleep 30\n\
java -jar config-server.jar &\n\
sleep 30\n\
java -jar api-gateway.jar &\n\
sleep 30\n\
java -jar auth-service.jar &\n\
java -jar user-service.jar &\n\
java -jar thesis-service.jar &\n\
java -jar profile-service.jar &\n\
java -jar group-service.jar &\n\
java -jar assign-service.jar &\n\
java -jar submission-service.jar &\n\
java -jar academic-config-service.jar &\n\
java -jar communication-log-service.jar &\n\
java -jar eval-service.jar &\n\
wait' > start-all.sh && chmod +x start-all.sh

EXPOSE 8080
CMD ["./start-all.sh"]
```

### Bước 2: Deploy lên Railway

```bash
# Deploy với Dockerfile
railway up
```

## Cấu hình Database

### PostgreSQL

- Tạo PostgreSQL database trên Railway
- Cấu hình `DATABASE_URL` environment variable
- Import `database-setup.sql` để tạo schema

### MongoDB

- Tạo MongoDB database trên Railway
- Cấu hình `MONGODB_URI` environment variable

### Redis

- Tạo Redis database trên Railway
- Cấu hình `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`

## Cấu hình Domain và SSL

1. Trên Railway Dashboard, vào Settings > Domains
2. Thêm custom domain (nếu có)
3. Railway tự động cung cấp SSL certificate

## Monitoring và Logs

- Sử dụng Railway Dashboard để monitor services
- Xem logs real-time cho mỗi service
- Cấu hình alerts nếu cần

## Troubleshooting

### Service không start được

1. Kiểm tra logs trên Railway Dashboard
2. Kiểm tra environment variables
3. Kiểm tra database connections

### Service không thể kết nối với nhau

1. Kiểm tra Eureka service discovery
2. Cập nhật service URLs trong config
3. Kiểm tra network policies

### Database connection issues

1. Kiểm tra database credentials
2. Kiểm tra database accessibility
3. Kiểm tra firewall rules

## Chi phí

- **Free Plan**: 1 service, 512MB RAM, 1GB storage
- **Pro Plan**: $5/tháng cho mỗi service
- **Database**: Riêng biệt, khoảng $5-20/tháng tùy loại

## Khuyến nghị

1. **Development**: Sử dụng Phương án 2 (monolith)
2. **Production**: Sử dụng Phương án 1 (microservices) với Pro Plan
3. **Testing**: Sử dụng Railway staging environment

## Liên kết hữu ích

- [Railway Documentation](https://docs.railway.app/)
- [Railway CLI Reference](https://docs.railway.app/develop/cli)
- [Environment Variables](https://docs.railway.app/develop/variables)
