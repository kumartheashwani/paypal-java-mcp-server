package com.example.mcpserver.service;

import com.example.mcpserver.model.McpRequest;
import com.example.mcpserver.model.McpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {

    private final ObjectMapper objectMapper;
    private final ToolRegistry toolRegistry;
    private final ToolExecutorService toolExecutorService;
    
    // Pattern to detect "improving authorization rate" or similar phrases
    private static final Pattern AUTH_RATE_PATTERN = Pattern.compile(
            "\\b(improv(e|ing)|increas(e|ing)|boost(ing)?|enhanc(e|ing))\\s+.{0,20}\\b(auth(orization)?|approval)\\s+.{0,10}\\b(rate|percentage|ratio)\\b",
            Pattern.CASE_INSENSITIVE);
            
    // Pattern to detect math operations
    private static final Pattern MATH_PATTERN = Pattern.compile(
            "\\b(calculat(e|or)|math|add|subtract|multiply|divide)\\b|\\b(\\d+)\\s*([+\\-*/×÷])\\s*(\\d+)\\b",
            Pattern.CASE_INSENSITIVE);

    public McpResponse processRequest(McpRequest request) {
        try {
            String query = extractQuery(request);
            log.debug("Processing query: {}", query);
            
            // Check if the query is about improving authorization rates
            if (isAuthorizationRateQuery(request)) {
                return handleAuthorizationRateQuery(request);
            }
            
            // Check if the query is about math calculations
            if (isMathQuery(request)) {
                return handleMathQuery(request);
            }
            
            // Default response for other queries
            return createDefaultResponse(query);
        } catch (Exception e) {
            log.error("Error processing MCP request", e);
            return McpResponse.builder()
                    .content("Error processing your request: " + e.getMessage())
                    .build();
        }
    }

    private String extractQuery(McpRequest request) {
        // Extract query from request
        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            return request.getQuery();
        }
        
        // Extract from messages if query is not directly provided
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (McpRequest.Message message : request.getMessages()) {
                if ("user".equals(message.getRole()) && message.getContent() != null) {
                    return message.getContent();
                }
            }
        }
        
        return "No query provided";
    }
    
    private boolean isAuthorizationRateQuery(McpRequest request) {
        // Check in query
        if (request.getQuery() != null && AUTH_RATE_PATTERN.matcher(request.getQuery()).find()) {
            return true;
        }
        
        // Check in user messages
        if (request.getMessages() != null) {
            for (McpRequest.Message mcpMessage : request.getMessages()) {
                if ("user".equals(mcpMessage.getRole()) && 
                        mcpMessage.getContent() != null && 
                        AUTH_RATE_PATTERN.matcher(mcpMessage.getContent()).find()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isMathQuery(McpRequest request) {
        // Check in query
        if (request.getQuery() != null && MATH_PATTERN.matcher(request.getQuery()).find()) {
            return true;
        }
        
        // Check in user messages
        if (request.getMessages() != null) {
            for (McpRequest.Message mcpMessage : request.getMessages()) {
                if ("user".equals(mcpMessage.getRole()) && 
                        mcpMessage.getContent() != null && 
                        MATH_PATTERN.matcher(mcpMessage.getContent()).find()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private McpResponse handleAuthorizationRateQuery(McpRequest request) {
        log.info("Handling authorization rate query");
        
        // Extract merchant ID if present
        String merchantId = extractMerchantId(request);
        
        if (merchantId == null) {
            // Ask for merchant ID if not provided
            return McpResponse.builder()
                    .content("To provide recommendations for improving your authorization rate, I need your merchant ID. Could you please provide it?")
                    .build();
        }
        
        try {
            // Create arguments for the tool
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("merchantId", merchantId);
            arguments.put("timeframe", "last_30_days");
            arguments.put("transactionType", "all");
            
            // Convert arguments to JSON
            String argsJson = objectMapper.writeValueAsString(arguments);
            
            // Execute the tool
            Object result = toolExecutorService.executeTool("improveAuthorizationRate", argsJson);
            
            // Format the result
            StringBuilder content = new StringBuilder();
            content.append("Based on the analysis of your authorization rates, here are the recommendations:\n\n");
            
            // Format the result nicely
            String resultJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            Map<String, Object> resultMap = objectMapper.readValue(resultJson, Map.class);
            
            // Extract and format current metrics
            Map<String, Object> metrics = (Map<String, Object>) resultMap.get("currentMetrics");
            content.append(String.format("Current Authorization Rate: %.1f%%\n", metrics.get("authorizationRate")));
            content.append(String.format("Current Decline Rate: %.1f%%\n", metrics.get("declineRate")));
            content.append(String.format("Total Transactions: %d\n\n", ((Number) metrics.get("totalTransactions")).intValue()));
            
            // Extract and format decline reasons
            content.append("Top Decline Reasons:\n");
            Map<String, Object> declineReasons = (Map<String, Object>) resultMap.get("declineReasons");
            declineReasons.forEach((reason, percentage) -> {
                content.append(String.format("- %s: %.1f%%\n", 
                        reason.replace("_", " "), ((Number) percentage).doubleValue()));
            });
            content.append("\n");
            
            // Extract and format recommendations
            content.append("Recommendations to Improve Authorization Rate:\n");
            List<Map<String, Object>> recommendations = (List<Map<String, Object>>) resultMap.get("recommendations");
            for (int i = 0; i < recommendations.size(); i++) {
                Map<String, Object> rec = recommendations.get(i);
                content.append(String.format("%d. %s (Priority: %s, Est. Impact: +%.1f%%)\n", 
                        i + 1, rec.get("title"), rec.get("priority"), ((Number) rec.get("estimatedImpactPercentage")).doubleValue()));
                content.append(String.format("   %s\n\n", rec.get("description")));
            }
            
            // Create tool call for metadata
            McpResponse.Function function = McpResponse.Function.builder()
                    .name("improveAuthorizationRate")
                    .arguments(argsJson)
                    .build();
            
            McpResponse.ToolCall toolCall = McpResponse.ToolCall.builder()
                    .id(UUID.randomUUID().toString())
                    .type("function")
                    .function(function)
                    .build();
            
            List<McpResponse.ToolCall> toolCalls = new ArrayList<>();
            toolCalls.add(toolCall);
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("merchantId", merchantId);
            metadata.put("timeframe", "last_30_days");
            
            return McpResponse.builder()
                    .content(content.toString())
                    .toolCalls(toolCalls)
                    .metadata(metadata)
                    .build();
        } catch (Exception e) {
            log.error("Error executing authorization rate tool", e);
            return McpResponse.builder()
                    .content("Error analyzing authorization rates: " + e.getMessage())
                    .build();
        }
    }
    
    private McpResponse handleMathQuery(McpRequest request) {
        log.info("Handling math query");
        
        String query = extractQuery(request);
        
        // Try to extract operation and operands from the query
        String operation = null;
        Double a = null;
        Double b = null;
        
        // Check for explicit operation mention
        if (query.toLowerCase().contains("add")) {
            operation = "add";
        } else if (query.toLowerCase().contains("subtract")) {
            operation = "subtract";
        } else if (query.toLowerCase().contains("multiply")) {
            operation = "multiply";
        } else if (query.toLowerCase().contains("divide")) {
            operation = "divide";
        }
        
        // Check for mathematical expression (e.g., "5 + 3")
        Pattern expressionPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*([+\\-*/×÷])\\s*(\\d+(?:\\.\\d+)?)");
        Matcher matcher = expressionPattern.matcher(query);
        
        if (matcher.find()) {
            a = Double.parseDouble(matcher.group(1));
            String symbol = matcher.group(2);
            b = Double.parseDouble(matcher.group(3));
            
            // Map symbol to operation
            switch (symbol) {
                case "+": operation = "add"; break;
                case "-": operation = "subtract"; break;
                case "*":
                case "×": operation = "multiply"; break;
                case "/":
                case "÷": operation = "divide"; break;
            }
        }
        
        if (operation == null) {
            // If we couldn't determine the operation, ask for clarification
            return McpResponse.builder()
                    .content("I can perform basic math operations (add, subtract, multiply, divide). " +
                            "Please specify the operation and the numbers you want to calculate. " +
                            "For example: 'add 5 and 3' or '5 + 3'.")
                    .build();
        }
        
        if (a == null || b == null) {
            // Extract numbers from the query
            List<Double> numbers = extractNumbers(query);
            
            if (numbers.size() >= 2) {
                a = numbers.get(0);
                b = numbers.get(1);
            } else {
                // Not enough numbers provided
                return McpResponse.builder()
                        .content("I need two numbers to perform a " + operation + " operation. " +
                                "Please provide both numbers. For example: '" + operation + " 5 and 3'.")
                        .build();
            }
        }
        
        try {
            // Create arguments for the tool
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("operation", operation);
            arguments.put("a", a);
            arguments.put("b", b);
            
            // Convert arguments to JSON
            String argsJson = objectMapper.writeValueAsString(arguments);
            
            // Execute the tool
            Object result = toolExecutorService.executeTool("calculate", argsJson);
            
            // Format the result
            String resultJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            Map<String, Object> resultMap = objectMapper.readValue(resultJson, Map.class);
            
            // Create tool call for metadata
            McpResponse.Function function = McpResponse.Function.builder()
                    .name("calculate")
                    .arguments(argsJson)
                    .build();
            
            McpResponse.ToolCall toolCall = McpResponse.ToolCall.builder()
                    .id(UUID.randomUUID().toString())
                    .type("function")
                    .function(function)
                    .build();
            
            List<McpResponse.ToolCall> toolCalls = new ArrayList<>();
            toolCalls.add(toolCall);
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("operation", operation);
            metadata.put("a", a);
            metadata.put("b", b);
            metadata.put("result", resultMap.get("result"));
            
            return McpResponse.builder()
                    .content("The result of " + resultMap.get("expression") + "")
                    .toolCalls(toolCalls)
                    .metadata(metadata)
                    .build();
        } catch (Exception e) {
            log.error("Error executing calculator tool", e);
            return McpResponse.builder()
                    .content("Error performing calculation: " + e.getMessage())
                    .build();
        }
    }
    
    private List<Double> extractNumbers(String text) {
        List<Double> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+(?:\\.\\d+)?");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        
        return numbers;
    }
    
    private String extractMerchantId(McpRequest request) {
        // Simple pattern to extract merchant ID
        Pattern merchantIdPattern = Pattern.compile("\\b(?:merchant|merch)\\s*(?:id|ID)?\\s*(?:is|:)?\\s*([A-Z0-9]{5,})", 
                Pattern.CASE_INSENSITIVE);
        
        // Check in query
        if (request.getQuery() != null) {
            Matcher matcher = merchantIdPattern.matcher(request.getQuery());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        // Check in messages
        if (request.getMessages() != null) {
            for (McpRequest.Message message : request.getMessages()) {
                if (message.getContent() != null) {
                    Matcher matcher = merchantIdPattern.matcher(message.getContent());
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        }
        
        // Check in context
        if (request.getContext() != null) {
            Object merchantIdObj = request.getContext().get("merchantId");
            if (merchantIdObj != null) {
                return merchantIdObj.toString();
            }
        }
        
        return null;
    }
    
    private McpResponse createDefaultResponse(String query) {
        // Simple response for non-specific queries
        String content = "I'm a PayPal MCP server that can help with authorization rates and perform basic math calculations. " +
                "You can ask me to improve your authorization rate (provide your merchant ID) or " +
                "perform calculations like 'add 5 and 3' or '5 + 3'.";
        
        if (query.toLowerCase().contains("hello") || query.toLowerCase().contains("hi")) {
            content = "Hello! " + content;
        } else if (query.toLowerCase().contains("help")) {
            content = "I can help you with the following:\n\n" +
                    "1. Improve your PayPal authorization rates - Just ask about improving your authorization rate and provide your merchant ID.\n" +
                    "2. Perform basic math calculations - You can ask me to add, subtract, multiply, or divide numbers.";
        }
        
        return McpResponse.builder()
                .content(content)
                .build();
    }
} 