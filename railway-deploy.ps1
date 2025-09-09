# Railway deployment script for Phenikaa Thesis Management (Windows PowerShell)
# This script helps deploy the application to Railway

param(
    [string]$ServiceName = "all",
    [switch]$Build = $false,
    [switch]$Help = $false
)

# Colors for output
$Red = "`e[31m"
$Green = "`e[32m"
$Yellow = "`e[33m"
$Blue = "`e[34m"
$Reset = "`e[0m"

function Write-ColorOutput {
    param([string]$Message, [string]$Color = $Reset)
    Write-Host "$Color$Message$Reset"
}

function Show-Help {
    Write-ColorOutput "Railway Deployment Script for Phenikaa Thesis Management" $Blue
    Write-ColorOutput "=======================================================" $Blue
    Write-Host ""
    Write-Host "Usage: .\railway-deploy.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -ServiceName <name>  Deploy specific service (default: all)"
    Write-Host "  -Build              Build the project before deployment"
    Write-Host "  -Help               Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\railway-deploy.ps1 -Build                    # Build and deploy all services"
    Write-Host "  .\railway-deploy.ps1 -ServiceName discovery-server  # Deploy only discovery server"
    Write-Host ""
    Write-Host "Available services:"
    Write-Host "  - discovery-server"
    Write-Host "  - config-server"
    Write-Host "  - api-gateway"
    Write-Host "  - auth-service"
    Write-Host "  - user-service"
    Write-Host "  - thesis-service"
    Write-Host "  - profile-service"
    Write-Host "  - group-service"
    Write-Host "  - assign-service"
    Write-Host "  - submission-service"
    Write-Host "  - academic-config-service"
    Write-Host "  - communication-log-service"
    Write-Host "  - eval-service"
}

if ($Help) {
    Show-Help
    exit 0
}

Write-ColorOutput "🚀 Railway Deployment Script for Phenikaa Thesis Management" $Green
Write-ColorOutput "=======================================================" $Green

# Check if Railway CLI is installed
try {
    $railwayVersion = railway --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Railway CLI not found"
    }
    Write-ColorOutput "✅ Railway CLI found: $railwayVersion" $Green
} catch {
    Write-ColorOutput "❌ Railway CLI is not installed. Please install it first:" $Red
    Write-Host "npm install -g @railway/cli"
    Write-Host "railway login"
    exit 1
}

# Check if user is logged in
try {
    $whoami = railway whoami 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Not logged in"
    }
    Write-ColorOutput "✅ Logged in as: $whoami" $Green
} catch {
    Write-ColorOutput "❌ Please login to Railway first:" $Red
    Write-Host "railway login"
    exit 1
}

# Build the project if requested
if ($Build) {
    Write-ColorOutput "🔨 Building the project..." $Yellow
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "❌ Build failed!" $Red
        exit 1
    }
    Write-ColorOutput "✅ Build completed successfully!" $Green
}

# Function to deploy a service
function Deploy-Service {
    param(
        [string]$ServiceName,
        [string]$JarPath,
        [int]$Port
    )
    
    Write-ColorOutput "📦 Deploying $ServiceName..." $Yellow
    
    # Create temporary directory
    $tempDir = "temp_$ServiceName"
    if (Test-Path $tempDir) {
        Remove-Item -Recurse -Force $tempDir
    }
    New-Item -ItemType Directory -Path $tempDir | Out-Null
    
    try {
        # Check if JAR file exists
        if (-not (Test-Path $JarPath)) {
            Write-ColorOutput "❌ JAR file not found: $JarPath" $Red
            return
        }
        
        # Copy JAR file
        Copy-Item $JarPath "$tempDir/app.jar"
        
        # Create Dockerfile
        $dockerfileContent = @"
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file
COPY app.jar app.jar

# Expose port
EXPOSE $Port

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:$Port/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
"@
        
        $dockerfileContent | Out-File -FilePath "$tempDir/Dockerfile" -Encoding UTF8
        
        # Deploy to Railway
        Set-Location $tempDir
        railway up --service $ServiceName --detach
        
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "✅ $ServiceName deployed successfully!" $Green
        } else {
            Write-ColorOutput "❌ Failed to deploy $ServiceName" $Red
        }
        
    } finally {
        # Clean up
        Set-Location ..
        if (Test-Path $tempDir) {
            Remove-Item -Recurse -Force $tempDir
        }
    }
}

# Deploy specific service or all services
if ($ServiceName -eq "all") {
    Write-ColorOutput "🚀 Starting deployment of all services..." $Yellow
    
    # Check if JAR files exist
    $services = @(
        @{Name="discovery-server"; Jar="discovery/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar"; Port=8761},
        @{Name="config-server"; Jar="config/config-server/target/config-server-0.0.1-SNAPSHOT.jar"; Port=8888},
        @{Name="api-gateway"; Jar="gateway/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar"; Port=8080},
        @{Name="auth-service"; Jar="services/auth-service/target/auth-service-1.0-SNAPSHOT.jar"; Port=8090},
        @{Name="user-service"; Jar="services/user-service/target/user-service-1.0-SNAPSHOT.jar"; Port=8081},
        @{Name="thesis-service"; Jar="services/thesis-service/target/thesis-service-1.0-SNAPSHOT.jar"; Port=8082},
        @{Name="profile-service"; Jar="services/profile-service/target/profile-service-1.0-SNAPSHOT.jar"; Port=8083},
        @{Name="group-service"; Jar="services/group-service/target/group-service-1.0-SNAPSHOT.jar"; Port=8084},
        @{Name="assign-service"; Jar="services/assign-service/target/assign-service-1.0-SNAPSHOT.jar"; Port=8085},
        @{Name="submission-service"; Jar="services/submission-service/target/submission-service-1.0-SNAPSHOT.jar"; Port=8086},
        @{Name="academic-config-service"; Jar="services/academic-config-service/target/academic-config-service-1.0-SNAPSHOT.jar"; Port=8087},
        @{Name="communication-log-service"; Jar="services/communication-log-service/target/communication-log-service-1.0-SNAPSHOT.jar"; Port=8088},
        @{Name="eval-service"; Jar="services/eval-service/target/eval-service-1.0-SNAPSHOT.jar"; Port=8089}
    )
    
    foreach ($service in $services) {
        if (Test-Path $service.Jar) {
            Deploy-Service -ServiceName $service.Name -JarPath $service.Jar -Port $service.Port
        } else {
            Write-ColorOutput "⚠️  JAR file not found: $($service.Jar)" $Yellow
        }
    }
    
} else {
    # Deploy specific service
    $serviceMap = @{
        "discovery-server" = @{Jar="discovery/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar"; Port=8761}
        "config-server" = @{Jar="config/config-server/target/config-server-0.0.1-SNAPSHOT.jar"; Port=8888}
        "api-gateway" = @{Jar="gateway/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar"; Port=8080}
        "auth-service" = @{Jar="services/auth-service/target/auth-service-1.0-SNAPSHOT.jar"; Port=8090}
        "user-service" = @{Jar="services/user-service/target/user-service-1.0-SNAPSHOT.jar"; Port=8081}
        "thesis-service" = @{Jar="services/thesis-service/target/thesis-service-1.0-SNAPSHOT.jar"; Port=8082}
        "profile-service" = @{Jar="services/profile-service/target/profile-service-1.0-SNAPSHOT.jar"; Port=8083}
        "group-service" = @{Jar="services/group-service/target/group-service-1.0-SNAPSHOT.jar"; Port=8084}
        "assign-service" = @{Jar="services/assign-service/target/assign-service-1.0-SNAPSHOT.jar"; Port=8085}
        "submission-service" = @{Jar="services/submission-service/target/submission-service-1.0-SNAPSHOT.jar"; Port=8086}
        "academic-config-service" = @{Jar="services/academic-config-service/target/academic-config-service-1.0-SNAPSHOT.jar"; Port=8087}
        "communication-log-service" = @{Jar="services/communication-log-service/target/communication-log-service-1.0-SNAPSHOT.jar"; Port=8088}
        "eval-service" = @{Jar="services/eval-service/target/eval-service-1.0-SNAPSHOT.jar"; Port=8089}
    }
    
    if ($serviceMap.ContainsKey($ServiceName)) {
        $serviceInfo = $serviceMap[$ServiceName]
        if (Test-Path $serviceInfo.Jar) {
            Deploy-Service -ServiceName $ServiceName -JarPath $serviceInfo.Jar -Port $serviceInfo.Port
        } else {
            Write-ColorOutput "❌ JAR file not found: $($serviceInfo.Jar)" $Red
            exit 1
        }
    } else {
        Write-ColorOutput "❌ Unknown service: $ServiceName" $Red
        Write-ColorOutput "Use -Help to see available services" $Yellow
        exit 1
    }
}

Write-ColorOutput "🎉 Deployment completed!" $Green
Write-ColorOutput "📝 Next steps:" $Yellow
Write-Host "1. Set up PostgreSQL database on Railway"
Write-Host "2. Set up MongoDB database on Railway"
Write-Host "3. Set up Redis database on Railway"
Write-Host "4. Configure environment variables for each service"
Write-Host "5. Update service URLs in configuration files"
Write-Host ""
Write-ColorOutput "📖 For detailed instructions, see RAILWAY_DEPLOYMENT.md" $Blue
