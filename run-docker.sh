#!/bin/bash

# This script runs the JSON-RPC stdio server in a Docker container

# Build the Docker image if it doesn't exist
if [[ "$(docker images -q paypal-mcp-jsonrpc 2> /dev/null)" == "" ]]; then
  echo "Building Docker image..."
  docker build -f Dockerfile.smithery -t paypal-mcp-jsonrpc .
fi

# Create logs directory if it doesn't exist
mkdir -p logs

echo "Starting JSON-RPC stdio server in Docker container..."

# Run the Docker container
docker run --rm \
  -v "$(pwd)/logs:/logs" \
  paypal-mcp-jsonrpc

echo "Docker container stopped" 