package com.example.mcpserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class McpException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus status;
    
    public McpException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    public McpException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
    
    public static McpException badRequest(String message) {
        return new McpException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }
    
    public static McpException unauthorized(String message) {
        return new McpException(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
    
    public static McpException modelError(String message) {
        return new McpException(message, "MODEL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 