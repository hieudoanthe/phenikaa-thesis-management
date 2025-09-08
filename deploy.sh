#!/bin/bash

# Deploy script for Phenikaa Thesis Management System
# Author: Phenikaa Team
# Version: 1.0

set -e

echo "🚀 Starting deployment process..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_status "Checking requirements..."
    
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 21 or higher."
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven."
        exit 1
    fi
    
    if ! command -v git &> /dev/null; then
        print_error "Git is not installed. Please install Git."
        exit 1
    fi
    
    print_success "All requirements are met!"
}

# Build all services
build_services() {
    print_status "Building all services..."
    
    # Build common-lib first
    print_status "Building common-lib..."
    cd common-lib
    mvn clean install -DskipTests
    cd ..
    
    # Build all other services
    print_status "Building discovery-server..."
    cd discovery/discovery-server
    mvn clean package -DskipTests
    cd ../..
    
    print_status "Building config-server..."
    cd config/config-server
    mvn clean package -DskipTests
    cd ../..
    
    print_status "Building api-gateway..."
    cd gateway/api-gateway
    mvn clean package -DskipTests
    cd ../..
    
    # Build all business services
    for service in auth-service user-service thesis-service profile-service group-service assign-service submission-service academic-config-service communication-log-service eval-service; do
        print_status "Building $service..."
        cd services/$service
        mvn clean package -DskipTests
        cd ../..
    done
    
    print_success "All services built successfully!"
}

# Deploy to Render.com
deploy_render() {
    print_status "Deploying to Render.com..."
    
    if [ ! -f "render.yaml" ]; then
        print_error "render.yaml not found. Please make sure it exists in the root directory."
        exit 1
    fi
    
    # Check if render CLI is installed
    if ! command -v render &> /dev/null; then
        print_warning "Render CLI not found. Please install it from https://render.com/docs/cli"
        print_status "You can also deploy manually by:"
        print_status "1. Go to https://dashboard.render.com/"
        print_status "2. Click 'New +' -> 'Blueprint'"
        print_status "3. Connect your GitHub repository"
        print_status "4. Render will automatically detect render.yaml"
        return
    fi
    
    # Deploy using render CLI
    render deploy
    
    print_success "Deployment to Render.com completed!"
}

# Deploy to Railway
deploy_railway() {
    print_status "Deploying to Railway..."
    
    if ! command -v railway &> /dev/null; then
        print_warning "Railway CLI not found. Please install it from https://docs.railway.app/develop/cli"
        return
    fi
    
    railway up
    
    print_success "Deployment to Railway completed!"
}

# Deploy to Heroku
deploy_heroku() {
    print_status "Deploying to Heroku..."
    
    if ! command -v heroku &> /dev/null; then
        print_warning "Heroku CLI not found. Please install it from https://devcenter.heroku.com/articles/heroku-cli"
        return
    fi
    
    # Check if git remote exists
    if ! git remote get-url heroku &> /dev/null; then
        print_status "Adding Heroku remote..."
        heroku create phenikaa-thesis-management
    fi
    
    git push heroku main
    
    print_success "Deployment to Heroku completed!"
}

# Local deployment
deploy_local() {
    print_status "Starting local deployment..."
    
    # Start services in background
    print_status "Starting Discovery Server..."
    cd discovery/discovery-server
    java -jar target/discovery-server-1.0-SNAPSHOT.jar &
    DISCOVERY_PID=$!
    cd ../..
    sleep 10
    
    print_status "Starting Config Server..."
    cd config/config-server
    java -jar target/config-server-0.0.1-SNAPSHOT.jar &
    CONFIG_PID=$!
    cd ../..
    sleep 10
    
    print_status "Starting API Gateway..."
    cd gateway/api-gateway
    java -jar target/api-gateway-1.0-SNAPSHOT.jar &
    GATEWAY_PID=$!
    cd ../..
    sleep 10
    
    print_status "Starting Auth Service..."
    cd services/auth-service
    java -jar target/auth-service-1.0-SNAPSHOT.jar &
    AUTH_PID=$!
    cd ../..
    sleep 5
    
    # Start other services
    for service in user-service thesis-service profile-service group-service assign-service submission-service academic-config-service communication-log-service eval-service; do
        print_status "Starting $service..."
        cd services/$service
        java -jar target/*.jar &
        cd ../..
        sleep 3
    done
    
    print_success "All services started locally!"
    print_status "Discovery Server: http://localhost:8761"
    print_status "Config Server: http://localhost:8888"
    print_status "API Gateway: http://localhost:8080"
    
    # Wait for user input to stop services
    print_warning "Press Ctrl+C to stop all services"
    trap 'kill $DISCOVERY_PID $CONFIG_PID $GATEWAY_PID $AUTH_PID; exit' INT
    wait
}

# Main deployment function
main() {
    echo "🎯 Phenikaa Thesis Management Deployment Script"
    echo "=============================================="
    
    # Parse command line arguments
    case "${1:-help}" in
        "render")
            check_requirements
            build_services
            deploy_render
            ;;
        "railway")
            check_requirements
            build_services
            deploy_railway
            ;;
        "heroku")
            check_requirements
            build_services
            deploy_heroku
            ;;
        "local")
            check_requirements
            build_services
            deploy_local
            ;;
        "build")
            check_requirements
            build_services
            ;;
        "help"|*)
            echo "Usage: $0 {render|railway|heroku|local|build|help}"
            echo ""
            echo "Commands:"
            echo "  render   - Deploy to Render.com"
            echo "  railway  - Deploy to Railway.app"
            echo "  heroku   - Deploy to Heroku"
            echo "  local    - Deploy locally"
            echo "  build    - Build all services only"
            echo "  help     - Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 render    # Deploy to Render.com"
            echo "  $0 local     # Run locally"
            echo "  $0 build     # Build only"
            ;;
    esac
}

# Run main function
main "$@"
