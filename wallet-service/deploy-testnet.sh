#!/bin/bash

# Exit on error
set -e

# Load environment variables
if [ -f .env.testnet ]; then
    source .env.testnet
else
    echo "Error: .env.testnet file not found"
    exit 1
fi

# Build the application
echo "Building application..."
./mvnw clean package -DskipTests

# Create deployment directory
DEPLOY_DIR="deploy/testnet"
mkdir -p $DEPLOY_DIR

# Copy necessary files
echo "Copying deployment files..."
cp target/wallet-service-*.jar $DEPLOY_DIR/
cp src/main/resources/application-testnet.yml $DEPLOY_DIR/application.yml
cp .env.testnet $DEPLOY_DIR/

# Create Dockerfile
cat > $DEPLOY_DIR/Dockerfile << EOL
FROM openjdk:17-slim

WORKDIR /app
COPY . .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "wallet-service-*.jar"]
EOL

# Build and push Docker image
echo "Building Docker image..."
docker build -t web3platform/wallet-service:testnet $DEPLOY_DIR
docker push web3platform/wallet-service:testnet

# Deploy to Kubernetes (if configured)
if [ -f $DEPLOY_DIR/k8s-deployment.yaml ]; then
    echo "Deploying to Kubernetes..."
    kubectl apply -f $DEPLOY_DIR/k8s-deployment.yaml
fi

# Run smoke tests
echo "Running smoke tests..."
./mvnw test -Dspring.profiles.active=testnet

echo "Deployment completed successfully!"