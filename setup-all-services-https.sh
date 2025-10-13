#!/bin/bash

echo "Configuring Nginx + SSL for all services with subdomains..."

# Cấu hình domain và email
DOMAIN="phenikaa-thesis-hdt.site" 
EMAIL="thehieu0814@gmail.com"

echo "Domain: $DOMAIN"
echo "Email: $EMAIL"
echo ""

# Bước 1: Cài đặt Nginx và Certbot
echo "Step 1: Installing Nginx and Certbot..."
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx

# Bước 2: Tạo Nginx config cho tất cả services với subdomain
echo "Step 2: Creating Nginx configuration for all services..."
sudo tee /etc/nginx/sites-available/phenikaa-all-services << EOF
# API Gateway (main domain)
server {
    listen 80;
    server_name $DOMAIN;
    
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

# Auth Service
server {
    listen 80;
    server_name auth.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8090;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# User Service
server {
    listen 80;
    server_name user.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Thesis Service
server {
    listen 80;
    server_name thesis.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8082;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Profile Service
server {
    listen 80;
    server_name profile.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8083;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Eval Service
server {
    listen 80;
    server_name eval.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8084;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Assign Service
server {
    listen 80;
    server_name assign.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8085;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Submission Service
server {
    listen 80;
    server_name submission.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8086;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Academic Config Service
server {
    listen 80;
    server_name academic.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8087;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Communication Log Service
server {
    listen 80;
    server_name communication.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8088;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # CORS headers
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
    }
}

# Eureka Dashboard
server {
    listen 80;
    server_name eureka.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8761;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}

# Config Server
server {
    listen 80;
    server_name config.$DOMAIN;
    
    location / {
        proxy_pass http://localhost:8888;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Bước 3: Enable site
echo "Step 3: Enabling Nginx site..."
sudo ln -sf /etc/nginx/sites-available/phenikaa-all-services /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Bước 4: Test Nginx config
echo "Step 4: Testing Nginx configuration..."
sudo nginx -t

if [ $? -ne 0 ]; then
    echo "Nginx configuration test failed!"
    exit 1
fi

# Bước 5: Start Nginx
echo "Step 5: Starting Nginx..."
sudo systemctl enable nginx
sudo systemctl start nginx

# Bước 6: Configure firewall
echo "Step 6: Configuring firewall..."
sudo ufw allow 'Nginx Full'
sudo ufw allow OpenSSH
sudo ufw --force enable

# Bước 7: Get SSL certificates for all domains
echo "Step 7: Getting SSL certificates for all domains..."
DOMAINS=(
    "$DOMAIN"
    "auth.$DOMAIN"
    "user.$DOMAIN"
    "thesis.$DOMAIN"
    "profile.$DOMAIN"
    "eval.$DOMAIN"
    "assign.$DOMAIN"
    "submission.$DOMAIN"
    "academic.$DOMAIN"
    "communication.$DOMAIN"
    "eureka.$DOMAIN"
    "config.$DOMAIN"
)

for domain in "${DOMAINS[@]}"; do
    echo "Getting SSL certificate for $domain..."
    sudo certbot --nginx -d $domain --email $EMAIL --agree-tos --non-interactive --redirect
done

# Bước 8: Setup auto-renewal
echo "Step 8: Setting up auto-renewal..."
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# Bước 9: Test SSL
echo "Step 9: Testing SSL..."
echo "Testing main domain:"
curl -I https://$DOMAIN

echo ""
echo "SSL configuration completed for all services!"
echo ""
echo "All services are now available at:"
echo "   - API Gateway: https://$DOMAIN"
echo "   - Auth Service: https://auth.$DOMAIN"
echo "   - User Service: https://user.$DOMAIN"
echo "   - Thesis Service: https://thesis.$DOMAIN"
echo "   - Profile Service: https://profile.$DOMAIN"
echo "   - Eval Service: https://eval.$DOMAIN"
echo "   - Assign Service: https://assign.$DOMAIN"
echo "   - Submission Service: https://submission.$DOMAIN"
echo "   - Academic Config Service: https://academic.$DOMAIN"
echo "   - Communication Log Service: https://communication.$DOMAIN"
echo "   - Eureka Dashboard: https://eureka.$DOMAIN"
echo "   - Config Server: https://config.$DOMAIN"
echo ""
echo "SSL certificates will auto-renew every 90 days."
echo ""
echo "Test commands:"
echo "   curl -I https://$DOMAIN"
echo "   curl -I https://$DOMAIN/actuator/health"
echo "   curl -I https://auth.$DOMAIN/actuator/health"
echo "   curl -I https://user.$DOMAIN/actuator/health"
