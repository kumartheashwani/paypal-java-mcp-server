#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Set environment variables
export SPRING_PROFILES_ACTIVE=stdio
export LOGGING_FILE_NAME=logs/mcp-server.log

# Run the JSON-RPC stdio server with explicit configuration
java \
  -Dspring.profiles.active=stdio \
  -Dlogging.file.name=logs/mcp-server.log \
  -Dlogging.level.com.example.mcpserver=DEBUG \
  -Dspring.jmx.enabled=false \
  -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration \
  -Dspring.mvc.async.request-timeout=60000 \
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar 