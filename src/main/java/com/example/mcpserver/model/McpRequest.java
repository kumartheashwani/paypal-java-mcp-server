package com.example.mcpserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpRequest {
    private String query;
    private List<Message> messages;
    private Map<String, Object> context;
    private Map<String, Object> options;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
        private Map<String, Object> metadata;
    }
} 