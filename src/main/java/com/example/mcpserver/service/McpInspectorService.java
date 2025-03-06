package com.example.mcpserver.service;

import com.example.mcpserver.model.McpInspectorData;
import com.example.mcpserver.model.McpRequest;
import com.example.mcpserver.model.McpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class McpInspectorService {

    // In-memory storage for MCP Inspector data
    private final Map<String, McpInspectorData> inspectorDataMap = new ConcurrentHashMap<>();
    
    // Maximum number of entries to keep
    private static final int MAX_ENTRIES = 100;
    
    /**
     * Records a new MCP request-response interaction
     * 
     * @param clientIp Client IP address
     * @param requestPath Request path
     * @param requestMethod Request method
     * @param request MCP request
     * @param response MCP response
     * @param processingTimeMs Processing time in milliseconds
     * @param additionalInfo Additional information
     * @return The ID of the recorded data
     */
    public String recordInteraction(
            String clientIp,
            String requestPath,
            String requestMethod,
            McpRequest request,
            McpResponse response,
            long processingTimeMs,
            Map<String, Object> additionalInfo) {
        
        String id = UUID.randomUUID().toString();
        
        McpInspectorData data = McpInspectorData.builder()
                .id(id)
                .timestamp(LocalDateTime.now())
                .clientIp(clientIp)
                .requestPath(requestPath)
                .requestMethod(requestMethod)
                .request(request)
                .response(response)
                .processingTimeMs(processingTimeMs)
                .additionalInfo(additionalInfo)
                .build();
        
        inspectorDataMap.put(id, data);
        log.debug("Recorded MCP interaction: {}", id);
        
        // Trim the map if it exceeds the maximum size
        if (inspectorDataMap.size() > MAX_ENTRIES) {
            trimOldestEntries();
        }
        
        return id;
    }
    
    /**
     * Gets all recorded MCP interactions
     * 
     * @return List of MCP Inspector data
     */
    public List<McpInspectorData> getAllInteractions() {
        return new ArrayList<>(inspectorDataMap.values())
                .stream()
                .sorted(Comparator.comparing(McpInspectorData::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets a specific MCP interaction by ID
     * 
     * @param id The ID of the interaction
     * @return The MCP Inspector data, or null if not found
     */
    public McpInspectorData getInteractionById(String id) {
        return inspectorDataMap.get(id);
    }
    
    /**
     * Clears all recorded MCP interactions
     */
    public void clearAllInteractions() {
        inspectorDataMap.clear();
        log.info("Cleared all MCP interactions");
    }
    
    /**
     * Removes the oldest entries to keep the map size within limits
     */
    private void trimOldestEntries() {
        List<Map.Entry<String, McpInspectorData>> entries = new ArrayList<>(inspectorDataMap.entrySet());
        entries.sort(Comparator.comparing(e -> e.getValue().getTimestamp()));
        
        int entriesToRemove = inspectorDataMap.size() - MAX_ENTRIES;
        for (int i = 0; i < entriesToRemove; i++) {
            inspectorDataMap.remove(entries.get(i).getKey());
        }
        
        log.debug("Trimmed {} oldest MCP interactions", entriesToRemove);
    }
} 