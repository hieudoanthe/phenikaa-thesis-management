#!/bin/bash

echo "Configuring HTTPS for all services with existing ports..."

# Bước 1: Tạo SSL certificate
echo "Step 1: Creating SSL certificate..."
mkdir -p ssl
openssl req -x509 -newkey rsa:4096 -keyout ssl/key.pem -out ssl/cert.pem -days 365 -nodes -subj "/C=VN/ST=Hanoi/L=Hanoi/O=Phenikaa/OU=IT/CN=163.61.110.164"
openssl pkcs12 -export -in ssl/cert.pem -inkey ssl/key.pem -out ssl/keystore.p12 -name "phenikaa" -password pass:changeit
echo "SSL certificate created"

# Bước 2: Cập nhật service URLs để sử dụng HTTPS
echo "Step 2: Updating service URLs to HTTPS..."
sed -i 's|http://auth-service:8090|https://auth-service:8090|g' docker-compose.yml
sed -i 's|http://user-service:8081|https://user-service:8081|g' docker-compose.yml
sed -i 's|http://thesis-service:8082|https://thesis-service:8082|g' docker-compose.yml
sed -i 's|http://profile-service:8083|https://profile-service:8083|g' docker-compose.yml
sed -i 's|http://eval-service:8084|https://eval-service:8084|g' docker-compose.yml
sed -i 's|http://assign-service:8085|https://assign-service:8085|g' docker-compose.yml
sed -i 's|http://submission-service:8086|https://submission-service:8086|g' docker-compose.yml
sed -i 's|http://academic-config-service:8087|https://academic-config-service:8087|g' docker-compose.yml
sed -i 's|http://communication-log-service:8088|https://communication-log-service:8088|g' docker-compose.yml
echo "Service URLs updated to HTTPS"

# Bước 3: Cập nhật .env
echo "Step 3: Updating environment variables..."
echo "" >> .env
echo "# HTTPS Configuration" >> .env
echo "SERVER_SSL_ENABLED=true" >> .env
echo "SERVER_SSL_KEY_STORE=/app/ssl/keystore.p12" >> .env
echo "SERVER_SSL_KEY_STORE_TYPE=PKCS12" >> .env
echo "SERVER_SSL_KEY_STORE_PASSWORD=changeit" >> .env
echo "SERVER_SSL_KEY_ALIAS=phenikaa" >> .env
echo "" >> .env
echo "# HTTPS Service URLs" >> .env
echo "AUTH_SERVICE_URL=https://auth-service:8090" >> .env
echo "USER_SERVICE_URL=https://user-service:8081" >> .env
echo "THESIS_SERVICE_URL=https://thesis-service:8082" >> .env
echo "PROFILE_SERVICE_URL=https://profile-service:8083" >> .env
echo "EVAL_SERVICE_URL=https://eval-service:8084" >> .env
echo "ASSIGN_SERVICE_URL=https://assign-service:8085" >> .env
echo "SUBMISSION_SERVICE_URL=https://submission-service:8086" >> .env
echo "ACADEMIC_CONFIG_SERVICE_URL=https://academic-config-service:8087" >> .env
echo "COMMUNICATION_LOG_SERVICE_URL=https://communication-log-service:8088" >> .env
echo "Environment variables updated"

# Bước 4: Rebuild và restart tất cả services
echo "Step 4: Rebuilding and restarting all services..."
docker-compose build
docker-compose down
docker-compose up -d

echo "Waiting for all services to start..."
sleep 60

# Bước 5: Test HTTPS endpoints
echo "Step 5: Testing HTTPS endpoints..."
echo "Testing API Gateway HTTPS:"
curl -k -s -o /dev/null -w "HTTPS Status: %{http_code}\n" https://localhost:8080/actuator/health

echo ""
echo "HTTPS configuration completed for all services!"
echo ""
echo "All services now use HTTPS with existing ports:"
echo "   - API Gateway: https://163.61.110.164:8080"
echo "   - Auth Service: https://163.61.110.164:8090"
echo "   - User Service: https://163.61.110.164:8081"
echo "   - Thesis Service: https://163.61.110.164:8082"
echo "   - Profile Service: https://163.61.110.164:8083"
echo "   - Eval Service: https://163.61.110.164:8084"
echo "   - Assign Service: https://163.61.110.164:8085"
echo "   - Submission Service: https://163.61.110.164:8086"
echo "   - Academic Config Service: https://163.61.110.164:8087"
echo "   - Communication Log Service: https://163.61.110.164:8088"
echo ""
echo "Test commands:"
echo "   curl -k https://163.61.110.164:8080/actuator/health"
echo "   curl -k https://163.61.110.164:8090/actuator/health"
