# 🚀 Deployment Guide - Phenikaa Thesis Management System

## 📋 Overview

This guide will help you deploy the Phenikaa Thesis Management System to various cloud platforms.

## 🏗️ Architecture

The system consists of 11 microservices:

- **Discovery Server** (Port 8761): Netflix Eureka Server
- **Config Server** (Port 8888): Spring Cloud Config Server
- **API Gateway** (Port 8080): Spring Cloud Gateway
- **Auth Service** (Port 8090): Authentication & Authorization
- **User Service** (Port 8081): User management
- **Thesis Service** (Port 8082): Thesis management + AI Chat
- **Profile Service** (Port 8083): Profile management
- **Group Service** (Port 8084): Group management
- **Assign Service** (Port 8085): Assignment management
- **Submission Service** (Port 8086): Submission management
- **Academic Config Service** (Port 8087): Academic configuration
- **Communication Log Service** (Port 8088): Chat & Notifications
- **Eval Service** (Port 8089): Evaluation management

## 🛠️ Prerequisites

- Java 21 or higher
- Maven 3.6+
- Git
- Database (PostgreSQL recommended for production)

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/hieudoanthe/phenikaa-thesis-management.git
cd phenikaa-thesis-management
```

### 2. Make Script Executable

```bash
chmod +x deploy.sh
```

### 3. Deploy to Render.com (Recommended)

```bash
./deploy.sh render
```

### 4. Deploy Locally

```bash
./deploy.sh local
```

## 🌐 Platform-Specific Deployment

### Render.com (Recommended)

1. **Automatic Deployment:**

   ```bash
   ./deploy.sh render
   ```

2. **Manual Deployment:**

   - Go to [Render.com](https://dashboard.render.com/)
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml`
   - Click "Apply" to deploy

3. **Environment Variables:**
   - Set `DB_USERNAME` and `DB_PASSWORD` for database access
   - Configure `JWT_SECRET` for security

### Railway.app

1. **Install Railway CLI:**

   ```bash
   npm install -g @railway/cli
   ```

2. **Deploy:**
   ```bash
   ./deploy.sh railway
   ```

### Heroku

1. **Install Heroku CLI:**

   ```bash
   # macOS
   brew install heroku/brew/heroku

   # Windows
   # Download from https://devcenter.heroku.com/articles/heroku-cli
   ```

2. **Deploy:**
   ```bash
   ./deploy.sh heroku
   ```

## 🗄️ Database Setup

### PostgreSQL (Recommended for Production)

1. **Create Databases:**

   ```sql
   CREATE DATABASE user_db;
   CREATE DATABASE thesis_db;
   CREATE DATABASE profile_db;
   CREATE DATABASE group_db;
   CREATE DATABASE assign_db;
   CREATE DATABASE submission_db;
   CREATE DATABASE academic_db;
   CREATE DATABASE evaluation_db;
   ```

2. **Environment Variables:**
   ```bash
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   export DB_HOST=your_host
   export DB_PORT=5432
   ```

### MongoDB (For Communication Log Service)

1. **Create Database:**

   ```javascript
   use communication_log_db
   ```

2. **Environment Variables:**
   ```bash
   export MONGODB_URI=mongodb://localhost:27017/phenikaa_thesis_connection
   ```

## 🔧 Configuration

### Environment Variables

| Variable                                | Description       | Default                         |
| --------------------------------------- | ----------------- | ------------------------------- |
| `SPRING_PROFILES_ACTIVE`                | Active profile    | `prod`                          |
| `PORT`                                  | Service port      | Service-specific                |
| `DB_USERNAME`                           | Database username | Required                        |
| `DB_PASSWORD`                           | Database password | Required                        |
| `DB_HOST`                               | Database host     | `localhost`                     |
| `DB_PORT`                               | Database port     | `5432`                          |
| `JWT_SECRET`                            | JWT secret key    | Default provided                |
| `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` | Eureka server URL | `http://localhost:8761/eureka/` |

### Service Dependencies

Deploy services in this order:

1. Discovery Server
2. Config Server
3. API Gateway
4. Auth Service
5. Other Business Services (can be parallel)

## 📊 Monitoring

### Health Checks

- Discovery Server: `http://localhost:8761/actuator/health`
- Config Server: `http://localhost:8888/actuator/health`
- API Gateway: `http://localhost:8080/actuator/health`
- Individual Services: `http://localhost:{port}/actuator/health`

### Logs

View logs using:

```bash
# Render.com
render logs --service service-name

# Railway
railway logs

# Heroku
heroku logs --tail
```

## 🐛 Troubleshooting

### Common Issues

1. **Build Failed:**

   - Check Java version (must be 21+)
   - Ensure Maven is installed
   - Check for compilation errors

2. **Database Connection:**

   - Verify database credentials
   - Check database server is running
   - Ensure network connectivity

3. **Service Discovery:**

   - Verify Discovery Server is running first
   - Check Eureka client configuration
   - Wait for services to register

4. **Memory Issues:**
   - Increase memory allocation
   - Check for memory leaks
   - Monitor resource usage

### Debug Commands

```bash
# Check service status
curl http://localhost:8080/actuator/health

# View service registry
curl http://localhost:8761/eureka/apps

# Check configuration
curl http://localhost:8888/{service-name}/{profile}
```

## 🔒 Security

### JWT Configuration

Update JWT secret in production:

```yaml
jwt:
  secret: your-secure-secret-key
  expiration: 86400000 # 24 hours
  refresh-expiration: 604800000 # 7 days
```

### Database Security

- Use strong passwords
- Enable SSL connections
- Restrict database access
- Regular security updates

## 📈 Scaling

### Horizontal Scaling

- Deploy multiple instances of each service
- Use load balancers
- Configure service discovery
- Monitor resource usage

### Vertical Scaling

- Increase memory allocation
- Optimize JVM settings
- Use connection pooling
- Cache frequently accessed data

## 🆘 Support

For issues and questions:

- Check logs for error messages
- Verify configuration settings
- Test individual services
- Contact development team

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.
