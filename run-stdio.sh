#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

# Set environment variables
export SPRING_PROFILES_ACTIVE=stdio
export LOGGING_FILE_NAME=logs/mcp-server.log

# Run the JSON-RPC stdio server with explicit configuration
java \
  -Dspring.profiles.active=stdio \
  -Dspring.main.web-application-type=NONE \
  -Dlogging.file.name=logs/mcp-server.log \
  -Dlogging.level.com.example.mcpserver=DEBUG \
  -Dlogging.pattern.console= \
  -Dlogging.config=classpath:logback-stdio.xml \
  -Dspring.jmx.enabled=false \
  -Dspring.main.lazy-initialization=true \
  -Dspring.mvc.async.request-timeout=60s \
  -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration,org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration,org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration \
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar 