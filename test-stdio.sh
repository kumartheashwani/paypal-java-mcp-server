#!/bin/bash

# This script tests the JSON-RPC stdio server by piping input to it
# It will send an initialize request, a getTools request, and a calculate request

# Create logs directory if it doesn't exist
mkdir -p logs

# Create a temporary file for input
cat > input.json << EOF
{"jsonrpc":"2.0","method":"initialize","id":"1"}
{"jsonrpc":"2.0","method":"getTools","id":"2"}
{"jsonrpc":"2.0","method":"executeFunction","params":{"function":"calculate","arguments":{"operation":"add","a":5,"b":3}},"id":"3"}
EOF

echo "Testing JSON-RPC stdio server..."
echo "Sending requests and capturing responses..."

# Run the server with input from the file
# Redirect stderr to a file to keep it separate from stdout
java \
  -Dspring.profiles.active=stdio \
  -Dspring.main.web-application-type=NONE \
  -DLOG_FILE=logs/mcp-server.log \
  -DLOG_LEVEL_COM_EXAMPLE_MCPSERVER=DEBUG \
  -Dlogging.pattern.console= \
  -Dlogging.config=classpath:logback-stdio.xml \
  -Dspring.jmx.enabled=false \
  -Dspring.main.lazy-initialization=true \
  -Dspring.mvc.async.request-timeout=60s \
  -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration \
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar < input.json 2>/dev/null

echo "Test completed."

# Clean up
rm input.json 