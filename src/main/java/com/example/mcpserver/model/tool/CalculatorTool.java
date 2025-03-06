package com.example.mcpserver.model.tool;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@SuperBuilder
public class CalculatorTool extends Tool {
    
    public static CalculatorTool createDefault() {
        Map<String, ParameterDefinition> parameters = new HashMap<>();
        
        parameters.put("operation", new ParameterDefinition(
            "string",
            "The operation to perform (add, subtract, multiply, divide)",
            true,
            null,
            new String[]{"add", "subtract", "multiply", "divide"}
        ));
        
        parameters.put("a", new ParameterDefinition(
            "number",
            "First operand",
            true,
            null,
            null
        ));
        
        parameters.put("b", new ParameterDefinition(
            "number",
            "Second operand",
            true,
            null,
            null
        ));
        
        return CalculatorTool.builder()
                .type("function")
                .function("calculate")
                .description("Performs basic math operations (add, subtract, multiply, divide)")
                .parameters(parameters)
                .build();
    }
} 