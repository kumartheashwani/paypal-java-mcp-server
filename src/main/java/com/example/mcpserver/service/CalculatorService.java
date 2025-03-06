package com.example.mcpserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorService {

    /**
     * Performs basic math operations
     * 
     * @param operation The operation to perform (add, subtract, multiply, divide)
     * @param a First operand
     * @param b Second operand
     * @return Map containing the result and operation details
     */
    public Map<String, Object> calculate(String operation, double a, double b) {
        log.info("Performing calculation: {} {} {}", a, operation, b);
        
        double result;
        
        switch (operation.toLowerCase()) {
            case "add":
                result = a + b;
                break;
            case "subtract":
                result = a - b;
                break;
            case "multiply":
                result = a * b;
                break;
            case "divide":
                if (b == 0) {
                    throw new ArithmeticException("Division by zero is not allowed");
                }
                result = a / b;
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("operation", operation);
        response.put("operand1", a);
        response.put("operand2", b);
        response.put("result", result);
        response.put("expression", String.format("%s %s %s = %s", a, getOperationSymbol(operation), b, result));
        
        return response;
    }
    
    private String getOperationSymbol(String operation) {
        switch (operation.toLowerCase()) {
            case "add": return "+";
            case "subtract": return "-";
            case "multiply": return "×";
            case "divide": return "÷";
            default: return operation;
        }
    }
} 