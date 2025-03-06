package com.example.mcpserver.service;

import com.example.mcpserver.model.tool.AuthorizationRateTool;
import com.example.mcpserver.model.tool.CalculatorTool;
import com.example.mcpserver.model.tool.Tool;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ToolRegistry {

    @Getter
    private final Map<String, Tool> tools = new HashMap<>();
    
    @Getter
    private final List<Tool> availableTools = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Register the authorization rate improvement tool
        AuthorizationRateTool authRateTool = AuthorizationRateTool.createDefault();
        registerTool(authRateTool);
        
        // Register the calculator tool
        CalculatorTool calculatorTool = CalculatorTool.createDefault();
        registerTool(calculatorTool);
        
        log.info("Initialized tool registry with {} tools", tools.size());
    }
    
    public void registerTool(Tool tool) {
        tools.put(tool.getFunction(), tool);
        availableTools.add(tool);
        log.debug("Registered tool: {}", tool.getFunction());
    }
    
    public Tool getTool(String function) {
        return tools.get(function);
    }
    
    public boolean hasTool(String function) {
        return tools.containsKey(function);
    }
} 