package com.example.mcpserver.jsonrpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRpcRequest {
    private String jsonrpc = "2.0";
    private String method;
    private Object params;
    private String id;
} 