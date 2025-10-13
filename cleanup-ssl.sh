#!/bin/bash

echo "Cleaning up SSL configuration for Nginx + Let's Encrypt setup..."

# Xóa thư mục SSL
if [ -d "ssl" ]; then
    echo "Removing ssl directory..."
    rm -rf ssl
    echo "SSL directory removed"
fi

# Xóa HTTPS environment variables khỏi .env
if [ -f ".env" ]; then
    echo "Cleaning .env file..."
    grep -v -E "(HTTPS|SSL)" .env > .env.clean
    mv .env.clean .env
    echo ".env file cleaned"
fi

# Xóa SSL volume mount khỏi docker-compose.yml
if [ -f "docker-compose.yml" ]; then
    echo "Cleaning docker-compose.yml..."
    # Backup original
    cp docker-compose.yml docker-compose.yml.backup
    
    # Remove SSL volume mount
    sed -i '/volumes:/,/environment:/c\    environment:' docker-compose.yml
    
    # Remove HTTPS environment variables
    sed -i '/# HTTPS Configuration/,/SERVER_SSL_KEY_ALIAS/d' docker-compose.yml
    
    echo "docker-compose.yml cleaned"
fi

# Xóa SSL configuration khỏi application.yml
if [ -f "gateway/api-gateway/src/main/resources/application.yml" ]; then
    echo "Cleaning application.yml..."
    # Backup original
    cp gateway/api-gateway/src/main/resources/application.yml gateway/api-gateway/src/main/resources/application.yml.backup
    
    # Remove SSL configuration
    sed -i '/ssl:/,/key-alias:/d' gateway/api-gateway/src/main/resources/application.yml
    
    echo "application.yml cleaned"
fi

echo ""
echo "Cleanup completed!"
echo ""
echo "Now you can use Nginx + Let's Encrypt setup:"
echo "   - Run: chmod +x setup-nginx-ssl.sh"
echo "   - Run: ./setup-nginx-ssl.sh"
echo ""
echo "This will:"
echo "   - Install Nginx and Certbot"
echo "   - Create SSL certificate from Let's Encrypt"
echo "   - Configure reverse proxy to API Gateway"
echo "   - Auto-renew SSL certificate"
echo ""
echo "No need for:"
echo "   - SSL certificates in project"
echo "   - HTTPS configuration in Spring Boot"
echo "   - SSL environment variables"
