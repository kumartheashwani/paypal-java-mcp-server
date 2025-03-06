#!/bin/bash

# Build the application
echo "Building application..."
mvn clean package -DskipTests

# Create Smithery deployment directory
echo "Creating Smithery deployment directory..."
mkdir -p smithery-deploy/logs

# Copy the JAR file
echo "Copying JAR file..."
cp target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar smithery-deploy/app.jar

# Copy the Smithery configuration
echo "Copying Smithery configuration..."
cp smithery-config.json smithery-deploy/

# Create a startup script
echo "Creating startup script..."
cat > smithery-deploy/start.sh << 'EOF'
#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Set environment variables
export SPRING_PROFILES_ACTIVE=stdio
export LOGGING_FILE_NAME=logs/mcp-server.log
export LOGGING_LEVEL_COM_EXAMPLE_MCPSERVER=DEBUG
export SPRING_JMX_ENABLED=false
export SPRING_MAIN_WEB_APPLICATION_TYPE=none
export SPRING_MVC_ASYNC_REQUEST_TIMEOUT=60s

# Run the JSON-RPC stdio server with explicit configuration
java \
  -Dspring.profiles.active=stdio \
  -Dspring.main.web-application-type=none \
  -Dlogging.file.name=logs/mcp-server.log \
  -Dlogging.level.com.example.mcpserver=DEBUG \
  -Dspring.jmx.enabled=false \
  -Dspring.mvc.async.request-timeout=60s \
  -jar app.jar
EOF

# Make the startup script executable
chmod +x smithery-deploy/start.sh

echo "Smithery deployment prepared in 'smithery-deploy' directory"
echo "To deploy to Smithery:"
echo "1. Upload the contents of 'smithery-deploy' to your Smithery server"
echo "2. Configure Smithery to use the provided configuration file"
echo "3. Start the service using: ./start.sh"
echo "   or with the command: java -Dspring.profiles.active=stdio -Dspring.main.web-application-type=none -Dlogging.file.name=logs/mcp-server.log -Dlogging.level.com.example.mcpserver=DEBUG -Dspring.jmx.enabled=false -Dspring.mvc.async.request-timeout=60s -jar app.jar" 