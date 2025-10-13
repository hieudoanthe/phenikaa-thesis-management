#!/bin/bash

echo "Configuring HTTPS for all services with existing ports..."

# Tạo thư mục SSL
mkdir -p ssl

# Tạo self-signed certificate
openssl req -x509 -newkey rsa:4096 -keyout ssl/key.pem -out ssl/cert.pem -days 365 -nodes -subj "/C=VN/ST=Hanoi/L=Hanoi/O=Phenikaa/OU=IT/CN=163.61.110.164"

# Tạo PKCS12 keystore
openssl pkcs12 -export -in ssl/cert.pem -inkey ssl/key.pem -out ssl/keystore.p12 -name "phenikaa" -password pass:changeit

echo "SSL certificate created for all services"
