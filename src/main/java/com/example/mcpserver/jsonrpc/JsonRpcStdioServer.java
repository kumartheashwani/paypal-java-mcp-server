package com.example.mcpserver.jsonrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@Profile("stdio")
public class JsonRpcStdioServer implements CommandLineRunner {

    private final JsonRpcHandler jsonRpcHandler;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final boolean interactiveMode;

    public JsonRpcStdioServer(JsonRpcHandler jsonRpcHandler, ObjectMapper objectMapper, Environment environment) {
        this.jsonRpcHandler = jsonRpcHandler;
        this.objectMapper = objectMapper;
        this.environment = environment;
        
        // Check if we're running in interactive mode
        this.interactiveMode = "true".equals(environment.getProperty("jsonrpc.stdio.interactive", "false"));
        
        // Log to stderr only
        log.info("JsonRpcStdioServer initialized (interactive mode: {})", interactiveMode);
    }

    @Override
    public void run(String... args) {
        // Log to stderr only
        log.info("Starting JSON-RPC stdio server");
        
        // Pre-initialize capabilities and tools to ensure they're loaded
        // even in non-interactive environments
        preInitialize();
        
        // Create a dedicated thread for handling stdin/stdout to avoid blocking the main thread
        Thread stdioThread = new Thread(this::processStdio, "stdio-processor");
        stdioThread.setDaemon(false);
        stdioThread.start();
        
        log.info("JSON-RPC stdio server started and ready to process requests");
        
        // In non-interactive mode, we don't wait for initialization
        // since we might never receive input
        if (interactiveMode) {
            // Wait for initialization to complete or timeout after 10 seconds
            try {
                if (!initLatch.await(10, TimeUnit.SECONDS)) {
                    log.warn("Server initialization timed out waiting for stdin input. " +
                             "This is normal in non-interactive environments. " +
                             "The server will continue running and process requests when they arrive.");
                }
            } catch (InterruptedException e) {
                log.warn("Server initialization was interrupted", e);
                Thread.currentThread().interrupt();
            }
        } else {
            log.info("Running in non-interactive mode, not waiting for stdin input");
            // In non-interactive mode, we consider the server initialized after pre-initialization
            initialized.set(true);
            initLatch.countDown();
        }
    }
    
    /**
     * Pre-initializes the server by loading capabilities and tools
     * This ensures the server is ready to respond even in non-interactive environments
     */
    private void preInitialize() {
        log.info("Pre-initializing server capabilities and tools");
        
        try {
            // Process a dummy initialize request to ensure capabilities are loaded
            String initRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":\"init\"}";
            log.debug("Processing initialization request: {}", initRequest);
            String response = jsonRpcHandler.handleRequest(initRequest);
            log.debug("Initialization response: {}", response);
            
            // Process a dummy getTools request to ensure tools are loaded
            String toolsRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"getTools\",\"id\":\"init-tools\"}";
            log.debug("Processing tools initialization request: {}", toolsRequest);
            response = jsonRpcHandler.handleRequest(toolsRequest);
            log.debug("Tools initialization response: {}", response);
            
            log.info("Server pre-initialization completed successfully");
        } catch (Exception e) {
            log.error("Error during pre-initialization", e);
        }
    }
    
    private void processStdio() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            log.debug("Stdio processor thread started");
            
            // Check if stdin is available/ready
            boolean stdinAvailable;
            try {
                stdinAvailable = reader.ready();
                log.info("Stdin availability check: {}", stdinAvailable ? "AVAILABLE" : "NOT AVAILABLE");
            } catch (Exception e) {
                stdinAvailable = false;
                log.warn("Error checking stdin availability: {}", e.getMessage());
            }
            
            if (!stdinAvailable && !interactiveMode) {
                log.warn("Stdin is not immediately available and running in non-interactive mode. " +
                         "The server will wait for input but may not receive any in non-interactive mode.");
            }
            
            String line;
            while (running.get()) {
                try {
                    // Try to read a line, which may block indefinitely in non-interactive environments
                    line = reader.readLine();
                    
                    // If we get null, it means EOF (end of stream)
                    if (line == null) {
                        log.info("Reached end of stdin stream, exiting");
                        break;
                    }
                    
                    // Skip empty lines
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    
                    // Mark as initialized after first successful read
                    if (!initialized.getAndSet(true)) {
                        log.info("First request received, server is now fully initialized");
                        initLatch.countDown();
                    }
                    
                    log.debug("Received line from stdin: {}", line);
                    String response = jsonRpcHandler.handleRequest(line);
                    
                    // Write directly to System.out for JSON-RPC responses
                    System.out.println(response);
                    System.out.flush();
                    
                    log.debug("Wrote response to stdout");
                } catch (Exception e) {
                    log.error("Error handling request or reading from stdin", e);
                    
                    // Only try to send an error response if it's a processing error, not an I/O error
                    if (initialized.get()) {
                        try {
                            JsonRpcResponse errorResponse = JsonRpcResponse.error(
                                    null, 
                                    -32603, 
                                    "Internal error", 
                                    e.getMessage()
                            );
                            String errorJson = objectMapper.writeValueAsString(errorResponse);
                            
                            // Write errors to System.out as well since they are part of the JSON-RPC protocol
                            System.out.println(errorJson);
                            System.out.flush();
                            
                            log.debug("Wrote error response to stdout: {}", errorJson);
                        } catch (Exception ex) {
                            log.error("Failed to write error response", ex);
                        }
                    }
                    
                    // If this is an I/O error and we haven't initialized yet, count down the latch
                    // to unblock the main thread
                    if (!initialized.get()) {
                        log.warn("I/O error before initialization, likely in a non-interactive environment");
                        initLatch.countDown();
                    }
                    
                    // If this is a fatal I/O error, break the loop
                    if (e instanceof java.io.IOException) {
                        log.error("Fatal I/O error, exiting stdio processor", e);
                        break;
                    }
                }
            }
            
            log.info("Stdio processor thread exiting");
        } catch (Exception e) {
            log.error("Fatal error in JSON-RPC stdio server", e);
        } finally {
            // Ensure the latch is counted down in case of errors
            initLatch.countDown();
        }
        
        log.info("JSON-RPC stdio server stopped");
    }
    
    public void stop() {
        log.info("Stopping JSON-RPC stdio server");
        running.set(false);
    }
} 