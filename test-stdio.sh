#!/bin/bash

# This script tests the JSON-RPC stdio server by piping input to it
# It will send an initialize request, a getTools request, and a calculate request

# Create logs directory if it doesn't exist
mkdir -p logs

# Run the server with input from a here document
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
  -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration \
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar << EOF
{"jsonrpc":"2.0","method":"initialize","id":"1"}
{"jsonrpc":"2.0","method":"getTools","id":"2"}
{"jsonrpc":"2.0","method":"executeFunction","params":{"function":"calculate","arguments":{"operation":"add","a":5,"b":3}},"id":"3"}
EOF 