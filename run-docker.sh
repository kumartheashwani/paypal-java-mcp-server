#!/bin/bash

# This script runs the JSON-RPC stdio server in a Docker container
# with the -i flag to ensure interactive mode

# Build the Docker image if it doesn't exist
if [[ "$(docker images -q paypal-mcp-jsonrpc 2> /dev/null)" == "" ]]; then
  echo "Building Docker image..."
  docker build -f Dockerfile.smithery -t paypal-mcp-jsonrpc .
fi

# Create logs directory if it doesn't exist
mkdir -p logs

echo "Starting JSON-RPC stdio server in Docker container..."
echo "IMPORTANT: The -i flag is required for the JSON-RPC stdio interface to work"

# Run the Docker container in interactive mode
# The -i flag is REQUIRED for the JSON-RPC stdio interface to work
docker run -i --rm \
  -v "$(pwd)/logs:/logs" \
  paypal-mcp-jsonrpc

echo "Docker container stopped" 