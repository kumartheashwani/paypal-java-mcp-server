#!/bin/bash

# This script tests the JSON-RPC stdio server directly
# It will send an initialize request and capture the response

# Create logs directory if it doesn't exist
mkdir -p logs

# Create a temporary file for input
echo '{"jsonrpc":"2.0","method":"initialize","id":"1"}' > input.json

# Run the server with input from the file and capture stdout
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
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar < input.json > output.json 2> error.log

echo "Output from JSON-RPC server:"
cat output.json

# Clean up
rm input.json 