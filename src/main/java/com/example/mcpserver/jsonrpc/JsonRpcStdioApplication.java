package com.example.mcpserver.jsonrpc;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.mcpserver")
public class JsonRpcStdioApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JsonRpcStdioApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setLogStartupInfo(false);
        app.setAddCommandLineProperties(false);
        
        // Set the active profile to stdio
        app.setAdditionalProfiles("stdio");
        
        app.run(args);
    }
} 