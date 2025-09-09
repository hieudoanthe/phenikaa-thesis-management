# Script to check if project is built and show JAR files
param([switch]$Build = $false)

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

Write-ColorOutput "🔍 Checking Phenikaa Thesis Management Build Status" $Blue
Write-ColorOutput "===============================================" $Blue

# Check if Maven is available
try {
    $mvnVersion = mvn --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
    Write-ColorOutput "✅ Maven found" $Green
} catch {
    Write-ColorOutput "❌ Maven is not installed or not in PATH" $Red
    exit 1
}

# Build if requested
if ($Build) {
    Write-ColorOutput "🔨 Building project..." $Yellow
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-ColorOutput "❌ Build failed!" $Red
        exit 1
    }
    Write-ColorOutput "✅ Build completed successfully!" $Green
}

# Check for JAR files
Write-ColorOutput "`n📦 Checking for JAR files..." $Yellow

$services = @(
    @{Name="discovery-server"; Path="discovery/discovery-server/target/discovery-server-1.0-SNAPSHOT.jar"},
    @{Name="config-server"; Path="config/config-server/target/config-server-1.0-SNAPSHOT.jar"},
    @{Name="api-gateway"; Path="gateway/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar"},
    @{Name="auth-service"; Path="services/auth-service/target/auth-service-1.0-SNAPSHOT.jar"},
    @{Name="user-service"; Path="services/user-service/target/user-service-1.0-SNAPSHOT.jar"},
    @{Name="thesis-service"; Path="services/thesis-service/target/thesis-service-1.0-SNAPSHOT.jar"},
    @{Name="profile-service"; Path="services/profile-service/target/profile-service-1.0-SNAPSHOT.jar"},
    @{Name="group-service"; Path="services/group-service/target/group-service-1.0-SNAPSHOT.jar"},
    @{Name="assign-service"; Path="services/assign-service/target/assign-service-1.0-SNAPSHOT.jar"},
    @{Name="submission-service"; Path="services/submission-service/target/submission-service-1.0-SNAPSHOT.jar"},
    @{Name="academic-config-service"; Path="services/academic-config-service/target/academic-config-service-1.0-SNAPSHOT.jar"},
    @{Name="communication-log-service"; Path="services/communication-log-service/target/communication-log-service-1.0-SNAPSHOT.jar"},
    @{Name="eval-service"; Path="services/eval-service/target/eval-service-1.0-SNAPSHOT.jar"}
)

$foundCount = 0
$missingCount = 0

foreach ($service in $services) {
    if (Test-Path $service.Path) {
        $fileInfo = Get-Item $service.Path
        $sizeKB = [math]::Round($fileInfo.Length / 1KB, 2)
        Write-ColorOutput "✅ $($service.Name) - $sizeKB KB" $Green
        $foundCount++
    } else {
        Write-ColorOutput "❌ $($service.Name) - NOT FOUND" $Red
        $missingCount++
    }
}

Write-ColorOutput "`n📊 Summary:" $Blue
Write-ColorOutput "Found: $foundCount services" $Green
Write-ColorOutput "Missing: $missingCount services" $Red

if ($missingCount -gt 0) {
    Write-ColorOutput "`n💡 To build the project, run:" $Yellow
    Write-Host ".\check-build.ps1 -Build"
    Write-Host "or"
    Write-Host "mvn clean package -DskipTests"
} else {
    Write-ColorOutput "`n🎉 All services are ready for deployment!" $Green
    Write-ColorOutput "You can now run: .\railway-deploy.ps1" $Yellow
}
