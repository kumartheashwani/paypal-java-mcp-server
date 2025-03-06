package com.example.mcpserver.controller;

import com.example.mcpserver.model.McpRequest;
import com.example.mcpserver.model.McpResponse;
import com.example.mcpserver.service.McpInspectorService;
import com.example.mcpserver.service.McpService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;
    private final McpInspectorService mcpInspectorService;

    @PostMapping("/completions")
    public ResponseEntity<McpResponse> getCompletions(
            @Valid @RequestBody McpRequest request,
            HttpServletRequest servletRequest) {
        log.debug("Received MCP request: {}", request);
        
        long startTime = System.currentTimeMillis();
        McpResponse response = mcpService.processRequest(request);
        long processingTime = System.currentTimeMillis() - startTime;
        
        // Record the interaction for the inspector
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("processingTimeMs", processingTime);
        additionalInfo.put("userAgent", servletRequest.getHeader("User-Agent"));
        
        mcpInspectorService.recordInteraction(
                servletRequest.getRemoteAddr(),
                servletRequest.getRequestURI(),
                servletRequest.getMethod(),
                request,
                response,
                processingTime,
                additionalInfo
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("MCP Server is running");
    }
} 