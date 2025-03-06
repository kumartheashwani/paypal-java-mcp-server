package com.example.mcpserver.service;

import com.example.mcpserver.exception.McpException;
import com.example.mcpserver.model.McpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutorService {

    private final AuthorizationRateService authorizationRateService;
    private final CalculatorService calculatorService;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    /**
     * Executes a tool based on the function name and arguments
     * 
     * @param functionName The name of the function to execute
     * @param arguments The arguments as a JSON string
     * @return The result of the tool execution
     */
    public Object executeTool(String functionName, String arguments) {
        log.info("Executing tool: {} with arguments: {}", functionName, arguments);
        
        if (!toolRegistry.hasTool(functionName)) {
            throw McpException.badRequest("Unknown tool function: " + functionName);
        }
        
        try {
            JsonNode argsNode = objectMapper.readTree(arguments);
            
            switch (functionName) {
                case "improveAuthorizationRate":
                    return executeImproveAuthorizationRate(argsNode);
                case "calculate":
                    return executeCalculate(argsNode);
                default:
                    throw McpException.badRequest("Tool function not implemented: " + functionName);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing tool arguments", e);
            throw McpException.badRequest("Invalid tool arguments: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error executing tool", e);
            throw McpException.modelError("Tool execution failed: " + e.getMessage());
        }
    }
    
    private Map<String, Object> executeImproveAuthorizationRate(JsonNode args) {
        String merchantId = args.has("merchantId") ? args.get("merchantId").asText() : null;
        String timeframe = args.has("timeframe") ? args.get("timeframe").asText() : "last_30_days";
        String transactionType = args.has("transactionType") ? args.get("transactionType").asText() : "all";
        
        if (merchantId == null || merchantId.isEmpty()) {
            throw McpException.badRequest("merchantId is required for improveAuthorizationRate tool");
        }
        
        return authorizationRateService.improveAuthorizationRate(merchantId, timeframe, transactionType);
    }
    
    private Map<String, Object> executeCalculate(JsonNode args) {
        String operation = args.has("operation") ? args.get("operation").asText() : null;
        Double a = args.has("a") ? args.get("a").asDouble() : null;
        Double b = args.has("b") ? args.get("b").asDouble() : null;
        
        if (operation == null || operation.isEmpty()) {
            throw McpException.badRequest("operation is required for calculate tool");
        }
        
        if (a == null) {
            throw McpException.badRequest("a (first operand) is required for calculate tool");
        }
        
        if (b == null) {
            throw McpException.badRequest("b (second operand) is required for calculate tool");
        }
        
        return calculatorService.calculate(operation, a, b);
    }
} 