package com.example.mcpserver.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@Profile("stdio")
public class JsonRpcStdioServer implements CommandLineRunner {

    private final JsonRpcHandler jsonRpcHandler;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public JsonRpcStdioServer(JsonRpcHandler jsonRpcHandler, ObjectMapper objectMapper) {
        this.jsonRpcHandler = jsonRpcHandler;
        this.objectMapper = objectMapper;
        log.info("JsonRpcStdioServer initialized");
    }

    @Override
    public void run(String... args) {
        log.info("Starting JSON-RPC stdio server");
        
        // Process a dummy initialize request to ensure capabilities are loaded
        try {
            String initRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":\"init\"}";
            log.debug("Processing initialization request: {}", initRequest);
            String response = jsonRpcHandler.handleRequest(initRequest);
            log.debug("Initialization response: {}", response);
        } catch (Exception e) {
            log.error("Error during initialization", e);
        }
        
        // Process a dummy getTools request to ensure tools are loaded
        try {
            String toolsRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"getTools\",\"id\":\"init-tools\"}";
            log.debug("Processing tools initialization request: {}", toolsRequest);
            String response = jsonRpcHandler.handleRequest(toolsRequest);
            log.debug("Tools initialization response: {}", response);
        } catch (Exception e) {
            log.error("Error during tools initialization", e);
        }
        
        // Create a dedicated thread for handling stdin/stdout to avoid blocking the main thread
        Thread stdioThread = new Thread(this::processStdio, "stdio-processor");
        stdioThread.setDaemon(false);
        stdioThread.start();
        
        log.info("JSON-RPC stdio server started and ready to process requests");
    }
    
    private void processStdio() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {
            
            log.debug("Stdio processor thread started");
            
            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    log.debug("Received line from stdin: {}", line);
                    String response = jsonRpcHandler.handleRequest(line);
                    writer.println(response);
                    writer.flush();
                    log.debug("Wrote response to stdout");
                } catch (Exception e) {
                    log.error("Error handling request", e);
                    try {
                        JsonRpcResponse errorResponse = JsonRpcResponse.error(
                                null, 
                                -32603, 
                                "Internal error", 
                                e.getMessage()
                        );
                        String errorJson = objectMapper.writeValueAsString(errorResponse);
                        writer.println(errorJson);
                        writer.flush();
                        log.debug("Wrote error response to stdout: {}", errorJson);
                    } catch (Exception ex) {
                        log.error("Failed to write error response", ex);
                    }
                }
            }
            
            log.info("Stdio processor thread exiting");
        } catch (Exception e) {
            log.error("Fatal error in JSON-RPC stdio server", e);
        }
        
        log.info("JSON-RPC stdio server stopped");
    }
    
    public void stop() {
        log.info("Stopping JSON-RPC stdio server");
        running.set(false);
    }
} 