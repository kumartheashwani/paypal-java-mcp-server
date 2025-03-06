#!/bin/bash

# Build the application
echo "Building application..."
mvn clean package -DskipTests

# Create Smithery deployment directory
echo "Creating Smithery deployment directory..."
mkdir -p smithery-deploy

# Copy the JAR file
echo "Copying JAR file..."
cp target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar smithery-deploy/app.jar

# Copy the Smithery configuration
echo "Copying Smithery configuration..."
cp smithery-config.json smithery-deploy/

echo "Smithery deployment prepared in 'smithery-deploy' directory"
echo "To deploy to Smithery:"
echo "1. Upload the contents of 'smithery-deploy' to your Smithery server"
echo "2. Configure Smithery to use the provided configuration file"
echo "3. Start the service using: java -Dspring.profiles.active=stdio -Dlogging.file.name=/logs/mcp-server.log -jar app.jar" 