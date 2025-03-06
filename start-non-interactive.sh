#!/bin/bash

# This script starts the JSON-RPC stdio server in non-interactive mode
# It creates named pipes for stdin/stdout and provides a simple HTTP endpoint
# to interact with the server

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

# Clean up pipes and background processes on exit
trap 'kill $SERVER_PID $READER_PID $WRITER_PID 2>/dev/null; rm -f "$INPUT_PIPE" "$OUTPUT_PIPE"; rmdir "$PIPE_DIR"' EXIT

# Start a background process to read from the output pipe
# This prevents the server from blocking when writing to stdout
cat "$OUTPUT_PIPE" > /dev/null &
READER_PID=$!

# Start a background process to write to the input pipe
# This ensures there's always a reader for the pipe
(
  # Send an initialize request to start
  echo '{"jsonrpc":"2.0","method":"initialize","id":"init"}'
  
  # Keep the pipe open
  while true; do
    sleep 3600
  done
) > "$INPUT_PIPE" &
WRITER_PID=$!

# Start the server with the pipes
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
  -Djsonrpc.stdio.interactive=false \
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar < "$INPUT_PIPE" > "$OUTPUT_PIPE" &

SERVER_PID=$!

echo "Server started with PID $SERVER_PID"
echo "Input pipe: $INPUT_PIPE"
echo "Output pipe: $OUTPUT_PIPE"
echo "You can now connect to these pipes to interact with the server."
echo "For example:"
echo "  To send a request: echo '{\"jsonrpc\":\"2.0\",\"method\":\"getTools\",\"id\":\"1\"}' > $INPUT_PIPE"
echo "  To read responses: cat $OUTPUT_PIPE"
echo ""
echo "Press Ctrl+C to stop the server"

# Wait for the server to exit or for the user to press Ctrl+C
wait $SERVER_PID 