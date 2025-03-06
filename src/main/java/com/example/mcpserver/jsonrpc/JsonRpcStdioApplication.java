package com.example.mcpserver.jsonrpc;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.mcpserver")
public class JsonRpcStdioApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JsonRpcStdioApplication.class);
        
        // Disable banner and startup info to keep stdout clean
        app.setBannerMode(Banner.Mode.OFF);
        app.setLogStartupInfo(false);
        
        // Explicitly set to non-web application to prevent web server startup
        app.setWebApplicationType(WebApplicationType.NONE);
        
        // Set the active profile to stdio
        app.setAdditionalProfiles("stdio");
        
        // Run the application
        app.run(args);
    }
} 