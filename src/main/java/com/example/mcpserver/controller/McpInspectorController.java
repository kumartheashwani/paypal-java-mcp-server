package com.example.mcpserver.controller;

import com.example.mcpserver.model.McpInspectorData;
import com.example.mcpserver.service.McpInspectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/inspector")
@RequiredArgsConstructor
public class McpInspectorController {

    private final McpInspectorService mcpInspectorService;

    @GetMapping
    public ResponseEntity<List<McpInspectorData>> getAllInteractions() {
        log.debug("Getting all MCP interactions");
        return ResponseEntity.ok(mcpInspectorService.getAllInteractions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<McpInspectorData> getInteractionById(@PathVariable String id) {
        log.debug("Getting MCP interaction by ID: {}", id);
        McpInspectorData data = mcpInspectorService.getInteractionById(id);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAllInteractions() {
        log.debug("Clearing all MCP interactions");
        mcpInspectorService.clearAllInteractions();
        return ResponseEntity.ok().build();
    }
} 