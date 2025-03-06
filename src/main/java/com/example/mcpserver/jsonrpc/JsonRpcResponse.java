package com.example.mcpserver.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    private String jsonrpc = "2.0";
    private Object result;
    private JsonRpcError error;
    private String id;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonRpcError {
        private int code;
        private String message;
        private Object data;
    }
    
    public static JsonRpcResponse success(String id, Object result) {
        return JsonRpcResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .result(result)
                .build();
    }
    
    public static JsonRpcResponse error(String id, int code, String message, Object data) {
        JsonRpcError error = JsonRpcError.builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
        
        return JsonRpcResponse.builder()
                .jsonrpc("2.0")
                .id(id)
                .error(error)
                .build();
    }
} 