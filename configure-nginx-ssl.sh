#!/bin/bash

echo "Configuring Nginx + Let's Encrypt SSL for VPS..."

# Kiểm tra domain
DOMAIN="163.61.110.164"  # Hoặc domain của bạn
EMAIL="thehieu0814@gmail.com"  # Thay bằng email thật

echo "Domain: $DOMAIN"
echo "Email: $EMAIL"

# Bước 1: Cài đặt Nginx
echo "Step 1: Installing Nginx..."
sudo apt update
sudo apt install -y nginx

# Bước 2: Cài đặt Certbot
echo "Step 2: Installing Certbot..."
sudo apt install -y certbot python3-certbot-nginx

# Bước 3: Tạo Nginx config cho API Gateway
echo "Step 3: Creating Nginx configuration..."
sudo tee /etc/nginx/sites-available/phenikaa-api << EOF
server {
    listen 80;
    server_name $DOMAIN;
    
    # Redirect HTTP to HTTPS
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name $DOMAIN;
    
    # SSL configuration will be added by Certbot
    
    # Proxy to API Gateway
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
        
        # Handle preflight requests
        if (\$request_method = 'OPTIONS') {
            add_header Access-Control-Allow-Origin *;
            add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
            add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
            add_header Access-Control-Max-Age 1728000;
            add_header Content-Type 'text/plain; charset=utf-8';
            add_header Content-Length 0;
            return 204;
        }
    }
    
    # WebSocket support
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Bước 4: Enable site
echo "Step 4: Enabling Nginx site..."
sudo ln -sf /etc/nginx/sites-available/phenikaa-api /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Bước 5: Test Nginx config
echo "Step 5: Testing Nginx configuration..."
sudo nginx -t

# Bước 6: Start Nginx
echo "Step 6: Starting Nginx..."
sudo systemctl enable nginx
sudo systemctl start nginx

# Bước 7: Configure firewall
echo "Step 7: Configuring firewall..."
sudo ufw allow 'Nginx Full'
sudo ufw allow OpenSSH
sudo ufw --force enable

# Bước 8: Get SSL certificate
echo "Step 8: Getting SSL certificate from Let's Encrypt..."
sudo certbot --nginx -d $DOMAIN --email $EMAIL --agree-tos --non-interactive --redirect

# Bước 9: Setup auto-renewal
echo "Step 9: Setting up auto-renewal..."
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# Bước 10: Test SSL
echo "Step 10: Testing SSL..."
curl -I https://$DOMAIN

echo ""
echo "SSL configuration completed!"
echo ""
echo "Your API Gateway is now available at:"
echo "   - HTTP: http://$DOMAIN (redirects to HTTPS)"
echo "   - HTTPS: https://$DOMAIN"
echo ""
echo "SSL certificate will auto-renew every 90 days."
echo ""
echo "Test commands:"
echo "   curl -I https://$DOMAIN"
echo "   curl -I https://$DOMAIN/actuator/health"
