package com.example.mcpserver.model.tool;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Tool {
    private String type;
    private String function;
    private String description;
    private Map<String, ParameterDefinition> parameters;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterDefinition {
        private String type;
        private String description;
        private boolean required;
        private Object defaultValue;
        private Object[] enumValues;
    }
} 