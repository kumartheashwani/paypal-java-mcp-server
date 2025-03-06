# PayPal Java MCP Server

A Java-based Model Context Protocol (MCP) server implementation using Spring Boot.

## Overview

This project implements a Model Context Protocol server that specializes in providing recommendations to improve PayPal authorization rates and performing basic math calculations. It provides both a RESTful API and a JSON-RPC over stdio interface for clients to send requests and receive responses.

## Features

- MCP-compliant API endpoints
- Support for context and metadata in requests
- Tool call handling
- Error handling and logging
- Authorization rate improvement tool
- Basic calculator tool
- JSON-RPC over stdio support for Smithery deployment

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Getting Started

### Configuration

Configure the application in `application.properties`:

```properties
# Server configuration
server.port=8080

# Logging
logging.level.com.example.mcpserver=DEBUG
```

### Building and Running

Build the application:

```bash
mvn clean package
```

#### Running as a Web Server

Run the application as a web server:

```bash
java -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT.jar
```

#### Running as a JSON-RPC stdio Server (for Smithery)

Run the application as a JSON-RPC stdio server:

```bash
./run-stdio.sh
```

Or directly with all required configuration parameters:

```bash
java \
  -Dspring.profiles.active=stdio \
  -Dlogging.file.name=logs/mcp-server.log \
  -Dlogging.level.com.example.mcpserver=DEBUG \
  -Dspring.jmx.enabled=false \
  -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration \
  -Dspring.mvc.async.request-timeout=60000 \
  -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT-stdio.jar
```

## API Usage

### REST API

#### Health Check

```bash
curl http://localhost:8080/api/v1/health
```

#### Completions Endpoint

```bash
curl -X POST http://localhost:8080/api/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How can I improve my authorization rate?",
    "messages": [
      {
        "role": "system",
        "content": "You are a helpful PayPal assistant."
      },
      {
        "role": "user",
        "content": "I need to improve my authorization rate. My merchant ID is MERCH123."
      }
    ],
    "context": {
      "merchantId": "MERCH123"
    }
  }'
```

#### Authorization Rate Improvement Tool

```bash
curl -X POST http://localhost:8080/api/v1/tools/improveAuthorizationRate/execute \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCH123",
    "timeframe": "last_30_days",
    "transactionType": "card"
  }'
```

#### Calculator Tool

```bash
curl -X POST http://localhost:8080/api/v1/tools/calculate/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "multiply",
    "a": 6,
    "b": 7
  }'
```

### JSON-RPC over stdio

The JSON-RPC over stdio interface accepts JSON-RPC 2.0 requests on stdin and writes responses to stdout. Each request and response is a single line of JSON.

#### Completions Request

```json
{"jsonrpc":"2.0","method":"completions","params":{"query":"How can I improve my authorization rate?","messages":[{"role":"system","content":"You are a helpful PayPal assistant."},{"role":"user","content":"I need to improve my authorization rate. My merchant ID is MERCH123."}],"context":{"merchantId":"MERCH123"}},"id":"1"}
```

#### Execute Function Request

```json
{"jsonrpc":"2.0","method":"executeFunction","params":{"function":"improveAuthorizationRate","arguments":{"merchantId":"MERCH123","timeframe":"last_30_days","transactionType":"card"}},"id":"2"}
```

```json
{"jsonrpc":"2.0","method":"executeFunction","params":{"function":"calculate","arguments":{"operation":"multiply","a":6,"b":7}},"id":"3"}
```

## Response Format

### REST API Response

```json
{
  "content": "Based on the analysis of your authorization rates, here are the recommendations:

Current Authorization Rate: 85.3%
Current Decline Rate: 14.7%
Total Transactions: 12500

Top Decline Reasons:
- insufficient funds: 42.5%
- risk triggers: 23.8%
- expired card: 12.3%
- invalid data: 10.7%
- other: 10.7%

Recommendations to Improve Authorization Rate:
1. Implement Account Updater (Priority: high, Est. Impact: +3.5%)
   Use PayPal's Account Updater service to automatically update expired or replaced cards

2. Optimize AVS Settings (Priority: medium, Est. Impact: +2.1%)
   Adjust Address Verification Service settings to reduce false declines

3. Implement Intelligent Retry Logic (Priority: high, Est. Impact: +4.2%)
   Add smart retry logic for declined transactions with specific reason codes

4. Review Risk Rules (Priority: medium, Est. Impact: +2.8%)
   Analyze and adjust risk rules to reduce false positives",
  "toolCalls": [
    {
      "id": "12345-67890",
      "type": "function",
      "function": {
        "name": "improveAuthorizationRate",
        "arguments": "{\"merchantId\":\"MERCH123\",\"timeframe\":\"last_30_days\",\"transactionType\":\"all\"}"
      }
    }
  ],
  "metadata": {
    "merchantId": "MERCH123",
    "timeframe": "last_30_days"
  }
}
```

### JSON-RPC Response

```json
{"jsonrpc":"2.0","result":{"content":"Based on the analysis of your authorization rates, here are the recommendations:\n\nCurrent Authorization Rate: 85.3%\nCurrent Decline Rate: 14.7%\nTotal Transactions: 12500\n\nTop Decline Reasons:\n- insufficient funds: 42.5%\n- risk triggers: 23.8%\n- expired card: 12.3%\n- invalid data: 10.7%\n- other: 10.7%\n\nRecommendations to Improve Authorization Rate:\n1. Implement Account Updater (Priority: high, Est. Impact: +3.5%)\n   Use PayPal's Account Updater service to automatically update expired or replaced cards\n\n2. Optimize AVS Settings (Priority: medium, Est. Impact: +2.1%)\n   Adjust Address Verification Service settings to reduce false declines\n\n3. Implement Intelligent Retry Logic (Priority: high, Est. Impact: +4.2%)\n   Add smart retry logic for declined transactions with specific reason codes\n\n4. Review Risk Rules (Priority: medium, Est. Impact: +2.8%)\n   Analyze and adjust risk rules to reduce false positives","toolCalls":[{"id":"12345-67890","type":"function","function":{"name":"improveAuthorizationRate","arguments":"{\"merchantId\":\"MERCH123\",\"timeframe\":\"last_30_days\",\"transactionType\":\"all\"}"}}],"metadata":{"merchantId":"MERCH123","timeframe":"last_30_days"}},"id":"1"}
```

## Available Tools

### Authorization Rate Improvement Tool

This tool analyzes transaction data and provides recommendations to improve authorization rates.

**Parameters:**
- `merchantId` (required): The merchant ID to analyze
- `timeframe` (optional): The timeframe for analysis (default: "last_30_days")
- `transactionType` (optional): Transaction type filter (default: "all")

### Calculator Tool

This tool performs basic math operations.

**Parameters:**
- `operation` (required): The operation to perform (add, subtract, multiply, divide)
- `a` (required): First operand
- `b` (required): Second operand

## Deploying to Smithery

To deploy this server to Smithery:

1. Use the provided preparation script:
   ```bash
   ./prepare-smithery.sh
   ```
   This will create a `smithery-deploy` directory with the necessary files.

2. Upload the contents of the `smithery-deploy` directory to your Smithery server.

3. Configure Smithery to use the provided `smithery-config.json` file.

4. Start the service using the provided script:
   ```bash
   ./start.sh
   ```

   Or with the explicit command:
   ```bash
   java \
     -Dspring.profiles.active=stdio \
     -Dlogging.file.name=logs/mcp-server.log \
     -Dlogging.level.com.example.mcpserver=DEBUG \
     -Dspring.jmx.enabled=false \
     -Dspring.autoconfigure.exclude=org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration \
     -Dspring.mvc.async.request-timeout=60000 \
     -jar app.jar
   ```

The Smithery configuration includes:
- Command: `java`
- Arguments: `-Dspring.profiles.active=stdio -Dlogging.file.name=/logs/mcp-server.log -jar /app/app.jar`
- Environment variables: `SPRING_PROFILES_ACTIVE=stdio`, `LOGGING_FILE_NAME=/logs/mcp-server.log`
- Capabilities: `completions`, `executeFunction`
- Tool definitions for `improveAuthorizationRate` and `calculate`

**Note**: The JSON-RPC stdio server relies on the `stdio` profile to configure itself correctly. The profile ensures that the server reads from stdin and writes to stdout while maintaining the necessary functionality.

## Docker Deployment

The project includes Docker support for both the REST API server and the JSON-RPC stdio server.

### Building Docker Images

#### Build Both Images Using Docker Compose

```bash
docker-compose build
```

#### Build REST API Server Image Only

```bash
docker build --target rest-api -t paypal-mcp-rest-api .
```

#### Build JSON-RPC Server Image Only

```bash
docker build -f Dockerfile.jsonrpc -t paypal-mcp-jsonrpc .
```

### Running with Docker

#### Run REST API Server

```bash
docker run -p 8080:8080 paypal-mcp-rest-api
```

#### Run JSON-RPC Server

The JSON-RPC server reads from stdin and writes to stdout, so it needs to be run with interactive mode:

```bash
docker run -i paypal-mcp-jsonrpc
```

Or use the provided script:

```bash
./run-docker-jsonrpc.sh
```

#### Using Docker Compose

Start the REST API server:

```bash
docker-compose up rest-api
```

Note: The JSON-RPC stdio server is not typically run directly with docker-compose since it requires stdin/stdout interaction.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
