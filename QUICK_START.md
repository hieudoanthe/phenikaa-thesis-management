# ⚡ Quick Start Guide

## 🚀 Deploy lên Render.com (5 phút)

### Bước 1: Push code lên GitHub

```bash
git add .
git commit -m "Add deployment configuration"
git push origin main
```

### Bước 2: Deploy trên Render.com

1. Truy cập [Render.com](https://dashboard.render.com/)
2. Đăng ký/Đăng nhập
3. Click "New +" → "Blueprint"
4. Connect GitHub repository: `hieudoanthe/phenikaa-thesis-management`
5. Render sẽ tự động detect file `render.yaml`
6. Click "Apply" để deploy

### Bước 3: Cấu hình Database

1. Tạo PostgreSQL Database trên Render
2. Cập nhật Environment Variables:
   - `DB_USERNAME`: username từ database
   - `DB_PASSWORD`: password từ database
   - `DB_HOST`: host từ connection string

## 🏠 Chạy Local (2 phút)

### Sử dụng Docker Compose

```bash
# Chạy tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f

# Dừng services
docker-compose down
```

### Sử dụng Script

```bash
# Build và chạy local
./deploy.sh local

# Chỉ build
./deploy.sh build
```

## 🔗 URLs sau khi deploy

- **Discovery Server**: `http://localhost:8761`
- **Config Server**: `http://localhost:8888`
- **API Gateway**: `http://localhost:8080`
- **Eureka Dashboard**: `http://localhost:8761`

## 🧪 Test API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Test authentication
curl -X POST http://localhost:8080/api/auth/login
```

## 🆘 Troubleshooting

### Lỗi thường gặp:

1. **Port đã được sử dụng**: Thay đổi port trong config
2. **Database connection**: Kiểm tra credentials
3. **Service không start**: Kiểm tra logs

### Debug:

```bash
# Xem logs của service
docker-compose logs service-name

# Restart service
docker-compose restart service-name
```

## 📞 Hỗ trợ

Nếu gặp vấn đề, kiểm tra:

1. Logs của services
2. Database connection
3. Network connectivity
4. Environment variables
