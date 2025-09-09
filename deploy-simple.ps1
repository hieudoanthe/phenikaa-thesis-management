# Simple deployment script for Railway
Write-Host "Deploying to Railway..." -ForegroundColor Blue

# Check if JAR file exists
$jarPath = "discovery/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "JAR file not found: $jarPath" -ForegroundColor Red
    exit 1
}

Write-Host "JAR file found: $jarPath" -ForegroundColor Green

# Create temporary directory
$tempDir = "temp_deploy"
if (Test-Path $tempDir) {
    Remove-Item -Recurse -Force $tempDir
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

# Copy JAR file
Copy-Item $jarPath "$tempDir/app.jar"

# Create Dockerfile
$dockerfileContent = "FROM eclipse-temurin:21-jre`n`nWORKDIR /app`n`n# Install curl for health checks`nRUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*`n`n# Copy the JAR file`nCOPY app.jar app.jar`n`n# Expose port`nEXPOSE 8761`n`n# Health check`nHEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \`n  CMD curl -f http://localhost:8761/actuator/health || exit 1`n`n# Run the application`nCMD [""java"", ""-jar"", ""app.jar""]"

$dockerfileContent | Out-File -FilePath "$tempDir/Dockerfile" -Encoding UTF8

# Deploy to Railway
Write-Host "Deploying to Railway..." -ForegroundColor Yellow
Set-Location $tempDir

# Use echo to automatically select the service
echo "phenikaa-thesis-management" | railway up --detach

if ($LASTEXITCODE -eq 0) {
    Write-Host "Deployment successful!" -ForegroundColor Green
    Write-Host "Check your Railway dashboard for the deployment URL" -ForegroundColor Blue
} else {
    Write-Host "Deployment failed" -ForegroundColor Red
}

# Clean up
Set-Location ..
if (Test-Path $tempDir) {
    Remove-Item -Recurse -Force $tempDir
}
