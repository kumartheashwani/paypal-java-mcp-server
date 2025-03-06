# PayPal Java MCP Server

A Java-based Model Context Protocol (MCP) server implementation using Spring Boot.

## Overview

This project implements a Model Context Protocol server that specializes in providing recommendations to improve PayPal authorization rates and performing basic math calculations. It provides a RESTful API for clients to send requests and receive responses.

## Features

- MCP-compliant API endpoints
- Support for context and metadata in requests
- Tool call handling
- Error handling and logging
- Authorization rate improvement tool
- Basic calculator tool

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

Run the application:

```bash
java -jar target/paypal-java-mcp-server-0.0.1-SNAPSHOT.jar
```

## API Usage

### Health Check

```bash
curl http://localhost:8080/api/v1/health
```

### Completions Endpoint

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

### Authorization Rate Improvement Tool

The server includes a special tool that is automatically triggered when a user mentions improving authorization rates. For example:

```bash
curl -X POST http://localhost:8080/api/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "query": "How can I improve my authorization rate for my merchant account?",
    "messages": [
      {
        "role": "system",
        "content": "You are a helpful PayPal assistant."
      },
      {
        "role": "user",
        "content": "I need to improve my authorization rate. My merchant ID is MERCH123."
      }
    ]
  }'
```

You can also directly invoke the tool:

```bash
curl -X POST http://localhost:8080/api/v1/tools/improveAuthorizationRate/execute \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCH123",
    "timeframe": "last_30_days",
    "transactionType": "card"
  }'
```

### Calculator Tool

The server includes a calculator tool that can perform basic math operations. It is automatically triggered when a user mentions math operations or uses mathematical expressions. For example:

```bash
curl -X POST http://localhost:8080/api/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What is 5 + 3?"
  }'
```

Or with more explicit operation naming:

```bash
curl -X POST http://localhost:8080/api/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
    "query": "Can you add 10 and 20?"
  }'
```

You can also directly invoke the calculator tool:

```bash
curl -X POST http://localhost:8080/api/v1/tools/calculate/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "multiply",
    "a": 6,
    "b": 7
  }'
```

## Response Format

### Authorization Rate Tool Response

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

### Calculator Tool Response

```json
{
  "content": "The result of 6 × 7 = 42.0",
  "toolCalls": [
    {
      "id": "67890-12345",
      "type": "function",
      "function": {
        "name": "calculate",
        "arguments": "{\"operation\":\"multiply\",\"a\":6,\"b\":7}"
      }
    }
  ],
  "metadata": {
    "operation": "multiply",
    "a": 6,
    "b": 7,
    "result": 42.0
  }
}
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

## License

This project is licensed under the MIT License - see the LICENSE file for details.
