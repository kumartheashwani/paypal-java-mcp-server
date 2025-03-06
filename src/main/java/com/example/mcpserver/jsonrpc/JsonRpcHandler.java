package com.example.mcpserver.jsonrpc;

import com.example.mcpserver.model.McpRequest;
import com.example.mcpserver.model.McpResponse;
import com.example.mcpserver.service.McpService;
import com.example.mcpserver.service.ToolExecutorService;
import com.example.mcpserver.service.ToolRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonRpcHandler {

    private final McpService mcpService;
    private final ToolExecutorService toolExecutorService;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;
    
    private static final int PARSE_ERROR = -32700;
    private static final int INVALID_REQUEST = -32600;
    private static final int METHOD_NOT_FOUND = -32601;
    private static final int INVALID_PARAMS = -32602;
    private static final int INTERNAL_ERROR = -32603;
    
    /**
     * Handles a JSON-RPC request
     * 
     * @param jsonRequest The JSON-RPC request as a string
     * @return The JSON-RPC response as a string
     */
    public String handleRequest(String jsonRequest) {
        log.debug("Received JSON-RPC request: {}", jsonRequest);
        try {
            JsonRpcRequest request = objectMapper.readValue(jsonRequest, JsonRpcRequest.class);
            JsonRpcResponse response = processRequest(request);
            String responseStr = objectMapper.writeValueAsString(response);
            log.debug("Sending JSON-RPC response: {}", responseStr);
            return responseStr;
        } catch (Exception e) {
            log.error("Error parsing JSON-RPC request", e);
            JsonRpcResponse response = JsonRpcResponse.error(null, PARSE_ERROR, "Parse error", e.getMessage());
            try {
                return objectMapper.writeValueAsString(response);
            } catch (Exception ex) {
                log.error("Error serializing error response", ex);
                return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32700,\"message\":\"Parse error\"},\"id\":null}";
            }
        }
    }
    
    /**
     * Processes a JSON-RPC request
     * 
     * @param request The JSON-RPC request
     * @return The JSON-RPC response
     */
    private JsonRpcResponse processRequest(JsonRpcRequest request) {
        if (request.getMethod() == null) {
            return JsonRpcResponse.error(request.getId(), INVALID_REQUEST, "Invalid request", "Method is required");
        }
        
        log.info("Processing JSON-RPC request: method={}, id={}", request.getMethod(), request.getId());
        
        try {
            switch (request.getMethod()) {
                case "completions":
                    return handleCompletions(request);
                case "executeFunction":
                    return handleExecuteFunction(request);
                case "getTools":
                    return handleGetTools(request);
                default:
                    return JsonRpcResponse.error(request.getId(), METHOD_NOT_FOUND, "Method not found", request.getMethod());
            }
        } catch (Exception e) {
            log.error("Error processing JSON-RPC request", e);
            return JsonRpcResponse.error(request.getId(), INTERNAL_ERROR, "Internal error", e.getMessage());
        }
    }
    
    /**
     * Handles a completions request
     * 
     * @param request The JSON-RPC request
     * @return The JSON-RPC response
     */
    private JsonRpcResponse handleCompletions(JsonRpcRequest request) {
        try {
            McpRequest mcpRequest = objectMapper.convertValue(request.getParams(), McpRequest.class);
            McpResponse mcpResponse = mcpService.processRequest(mcpRequest);
            return JsonRpcResponse.success(request.getId(), mcpResponse);
        } catch (Exception e) {
            log.error("Error handling completions request", e);
            return JsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Invalid params", e.getMessage());
        }
    }
    
    /**
     * Handles an execute function request
     * 
     * @param request The JSON-RPC request
     * @return The JSON-RPC response
     */
    private JsonRpcResponse handleExecuteFunction(JsonRpcRequest request) {
        try {
            JsonNode params = objectMapper.valueToTree(request.getParams());
            
            if (!params.has("function") || !params.has("arguments")) {
                return JsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Invalid params", 
                        "Both 'function' and 'arguments' are required");
            }
            
            String function = params.get("function").asText();
            String arguments = objectMapper.writeValueAsString(params.get("arguments"));
            
            Object result = toolExecutorService.executeTool(function, arguments);
            return JsonRpcResponse.success(request.getId(), result);
        } catch (Exception e) {
            log.error("Error handling execute function request", e);
            return JsonRpcResponse.error(request.getId(), INVALID_PARAMS, "Invalid params", e.getMessage());
        }
    }
    
    /**
     * Handles a get tools request
     * 
     * @param request The JSON-RPC request
     * @return The JSON-RPC response with the list of available tools
     */
    private JsonRpcResponse handleGetTools(JsonRpcRequest request) {
        try {
            log.info("Handling getTools request");
            return JsonRpcResponse.success(request.getId(), toolRegistry.getAvailableTools());
        } catch (Exception e) {
            log.error("Error handling get tools request", e);
            return JsonRpcResponse.error(request.getId(), INTERNAL_ERROR, "Internal error", e.getMessage());
        }
    }
} 