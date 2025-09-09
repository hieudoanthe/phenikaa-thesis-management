# Simple Discovery Server deployment script
Write-Host "🚀 Deploying Discovery Server to Railway" -ForegroundColor Blue

# Check if JAR file exists
$jarPath = "discovery/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "❌ JAR file not found: $jarPath" -ForegroundColor Red
    Write-Host "Please build the project first: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ JAR file found: $jarPath" -ForegroundColor Green

# Create temporary directory
$tempDir = "temp_discovery"
if (Test-Path $tempDir) {
    Remove-Item -Recurse -Force $tempDir
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

# Copy JAR file
Copy-Item $jarPath "$tempDir/app.jar"

# Create Dockerfile
$dockerfileContent = @"
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the JAR file
COPY app.jar app.jar

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
"@

$dockerfileContent | Out-File -FilePath "$tempDir/Dockerfile" -Encoding UTF8

# Deploy to Railway
Write-Host "📦 Deploying to Railway..." -ForegroundColor Yellow
Set-Location $tempDir
railway up --service discovery-server --detach

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Discovery Server deployed successfully!" -ForegroundColor Green
    Write-Host "🔗 Check your Railway dashboard for the deployment URL" -ForegroundColor Blue
} else {
    Write-Host "❌ Failed to deploy Discovery Server" -ForegroundColor Red
}

# Clean up
Set-Location ..
if (Test-Path $tempDir) {
    Remove-Item -Recurse -Force $tempDir
}

Write-Host ""
Write-Host "📝 Next steps:" -ForegroundColor Yellow
Write-Host "1. Wait for discovery server to be ready (check Railway dashboard)"
Write-Host "2. Note the discovery server URL"
Write-Host "3. Deploy config server next"
Write-Host "4. Update environment variables with the discovery server URL"
