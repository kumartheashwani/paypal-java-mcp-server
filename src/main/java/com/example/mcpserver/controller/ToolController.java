package com.example.mcpserver.controller;

import com.example.mcpserver.model.tool.Tool;
import com.example.mcpserver.service.ToolExecutorService;
import com.example.mcpserver.service.ToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolRegistry toolRegistry;
    private final ToolExecutorService toolExecutorService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<Tool>> getAvailableTools() {
        return ResponseEntity.ok(toolRegistry.getAvailableTools());
    }
    
    @GetMapping("/{function}")
    public ResponseEntity<Tool> getToolDefinition(@PathVariable String function) {
        Tool tool = toolRegistry.getTool(function);
        if (tool == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tool);
    }
    
    @PostMapping("/{function}/execute")
    public ResponseEntity<Object> executeToolDirectly(
            @PathVariable String function,
            @RequestBody Map<String, Object> arguments) {
        
        try {
            // Convert arguments to JSON string
            String argsJson = objectMapper.writeValueAsString(arguments);
            
            // Execute the tool
            Object result = toolExecutorService.executeTool(function, argsJson);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing tool directly", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tool execution failed",
                    "message", e.getMessage()
            ));
        }
    }
} 