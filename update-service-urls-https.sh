#!/bin/bash

echo "Updating all services to use HTTPS URLs..."

# Cập nhật service URLs trong docker-compose.yml để sử dụng HTTPS
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

# Cập nhật .env để thêm HTTPS URLs
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

echo "Environment variables updated for HTTPS"
