package com.example.mcpserver.model.tool;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@SuperBuilder
public class AuthorizationRateTool extends Tool {
    
    public static AuthorizationRateTool createDefault() {
        Map<String, ParameterDefinition> parameters = new HashMap<>();
        
        parameters.put("merchantId", new ParameterDefinition(
            "string",
            "The merchant ID to analyze",
            true,
            null,
            null
        ));
        
        parameters.put("timeframe", new ParameterDefinition(
            "string",
            "The timeframe for analysis",
            true,
            "last_30_days",
            new String[]{"last_7_days", "last_30_days", "last_90_days", "last_year"}
        ));
        
        parameters.put("transactionType", new ParameterDefinition(
            "string",
            "Optional transaction type filter",
            false,
            "all",
            new String[]{"all", "card", "bank", "wallet", "crypto"}
        ));
        
        return AuthorizationRateTool.builder()
                .type("function")
                .function("improveAuthorizationRate")
                .description("Analyzes transaction data and provides recommendations to improve authorization rates")
                .parameters(parameters)
                .build();
    }
} 