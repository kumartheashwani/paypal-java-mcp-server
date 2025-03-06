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
    }

    @Override
    public void run(String... args) {
        log.info("Starting JSON-RPC stdio server");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {
            
            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String response = jsonRpcHandler.handleRequest(line);
                    writer.println(response);
                    writer.flush();
                } catch (Exception e) {
                    log.error("Error handling request", e);
                    JsonRpcResponse errorResponse = JsonRpcResponse.error(
                            null, 
                            -32603, 
                            "Internal error", 
                            e.getMessage()
                    );
                    writer.println(objectMapper.writeValueAsString(errorResponse));
                    writer.flush();
                }
            }
        } catch (Exception e) {
            log.error("Error in JSON-RPC stdio server", e);
        }
        
        log.info("JSON-RPC stdio server stopped");
    }
    
    public void stop() {
        running.set(false);
    }
} 