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

# Copy the logback configuration
echo "Copying logback configuration..."
mkdir -p smithery-deploy/config
cp src/main/resources/logback-stdio.xml smithery-deploy/config/

# Create a startup script
echo "Creating startup script..."
cat > smithery-deploy/start.sh << 'EOF'
#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Set environment variables
export SPRING_PROFILES_ACTIVE=stdio
export LOG_FILE=logs/mcp-server.log
export LOG_LEVEL_COM_EXAMPLE_MCPSERVER=DEBUG
export LOGGING_PATTERN_CONSOLE=
export SPRING_JMX_ENABLED=false
export SPRING_MAIN_WEB_APPLICATION_TYPE=none
export SPRING_MVC_ASYNC_REQUEST_TIMEOUT=60s

# IMPORTANT: This script must be run in an environment where stdin/stdout are available
# for the JSON-RPC stdio interface to work correctly.
# If running in a non-interactive environment, consider using a tool like 'expect' or 'socat'
# to provide the necessary stdin/stdout connectivity.

# Run the JSON-RPC stdio server with explicit configuration
exec java \
  -Dspring.profiles.active=stdio \
  -Dspring.main.web-application-type=NONE \
  -DLOG_FILE=logs/mcp-server.log \
  -DLOG_LEVEL_COM_EXAMPLE_MCPSERVER=DEBUG \
  -Dlogging.pattern.console= \
  -Dlogging.config=config/logback-stdio.xml \
  -Dspring.jmx.enabled=false \
  -Dspring.main.lazy-initialization=true \
  -Dspring.mvc.async.request-timeout=60s \
  -jar app.jar
EOF

# Create a non-interactive startup script using named pipes
echo "Creating non-interactive startup script..."
cat > smithery-deploy/start-non-interactive.sh << 'EOF'
#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Set environment variables
export SPRING_PROFILES_ACTIVE=stdio
export LOG_FILE=logs/mcp-server.log
export LOG_LEVEL_COM_EXAMPLE_MCPSERVER=DEBUG
export LOGGING_PATTERN_CONSOLE=
export SPRING_JMX_ENABLED=false
export SPRING_MAIN_WEB_APPLICATION_TYPE=none
export SPRING_MVC_ASYNC_REQUEST_TIMEOUT=60s

# Create named pipes for stdin/stdout
PIPE_DIR=$(mktemp -d)
INPUT_PIPE="$PIPE_DIR/input_pipe"
OUTPUT_PIPE="$PIPE_DIR/output_pipe"

# Create the named pipes
mkfifo "$INPUT_PIPE"
mkfifo "$OUTPUT_PIPE"

# Clean up pipes on exit
trap 'rm -f "$INPUT_PIPE" "$OUTPUT_PIPE"; rmdir "$PIPE_DIR"' EXIT

# Start the server with the pipes
java \
  -Dspring.profiles.active=stdio \
  -Dspring.main.web-application-type=NONE \
  -DLOG_FILE=logs/mcp-server.log \
  -DLOG_LEVEL_COM_EXAMPLE_MCPSERVER=DEBUG \
  -Dlogging.pattern.console= \
  -Dlogging.config=config/logback-stdio.xml \
  -Dspring.jmx.enabled=false \
  -Dspring.main.lazy-initialization=true \
  -Dspring.mvc.async.request-timeout=60s \
  -jar app.jar < "$INPUT_PIPE" > "$OUTPUT_PIPE" &

SERVER_PID=$!

echo "Server started with PID $SERVER_PID"
echo "Input pipe: $INPUT_PIPE"
echo "Output pipe: $OUTPUT_PIPE"
echo "You can now connect to these pipes to interact with the server."
echo "For example:"
echo "  To send a request: echo '{\"jsonrpc\":\"2.0\",\"method\":\"getTools\",\"id\":\"1\"}' > $INPUT_PIPE"
echo "  To read responses: cat $OUTPUT_PIPE"

# Wait for the server to exit
wait $SERVER_PID
EOF

# Make the startup scripts executable
chmod +x smithery-deploy/start.sh
chmod +x smithery-deploy/start-non-interactive.sh

# Create a Docker run script
echo "Creating Docker run script..."
cat > smithery-deploy/run-docker.sh << 'EOF'
#!/bin/bash

# Build the Docker image
docker build -f Dockerfile.smithery -t paypal-mcp-jsonrpc .

# Run the Docker container in interactive mode
# The -i flag is REQUIRED for the JSON-RPC stdio interface to work
docker run -i --rm \
  -v "$(pwd)/logs:/logs" \
  paypal-mcp-jsonrpc
EOF

# Make the Docker run script executable
chmod +x smithery-deploy/run-docker.sh

echo "Smithery deployment prepared in 'smithery-deploy' directory"
echo "To deploy to Smithery:"
echo "1. Upload the contents of 'smithery-deploy' to your Smithery server"
echo "2. Configure Smithery to use the provided configuration file"
echo "3. Start the service using one of the following methods:"
echo "   a. Interactive mode: ./start.sh"
echo "   b. Non-interactive mode: ./start-non-interactive.sh"
echo "   c. Docker: ./run-docker.sh"
echo ""
echo "IMPORTANT: The JSON-RPC stdio interface requires stdin/stdout connectivity."
echo "If running in a non-interactive environment, use the start-non-interactive.sh script"
echo "which creates named pipes that you can connect to." 