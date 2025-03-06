#!/bin/bash

# Build the JSON-RPC server Docker image
echo "Building JSON-RPC server Docker image..."
docker build -f Dockerfile.jsonrpc -t paypal-mcp-jsonrpc .

# Run the JSON-RPC server in interactive mode
echo "Starting JSON-RPC server..."
echo "Type JSON-RPC requests or press Ctrl+C to exit."
echo ""
docker run -i paypal-mcp-jsonrpc 