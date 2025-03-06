package com.example.mcpserver.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpInspectorData {
    private String id;
    private LocalDateTime timestamp;
    private String clientIp;
    private String requestPath;
    private String requestMethod;
    private McpRequest request;
    private McpResponse response;
    private long processingTimeMs;
    private Map<String, Object> additionalInfo;
} 