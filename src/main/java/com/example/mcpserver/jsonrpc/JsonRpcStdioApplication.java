package com.example.mcpserver.jsonrpc;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication(exclude = {
    WebMvcAutoConfiguration.class
})
@ComponentScan(basePackages = "com.example.mcpserver")
public class JsonRpcStdioApplication {

    public static void main(String[] args) {
        // Configure Spring Boot to not use stdout for logging
        System.setProperty("logging.pattern.console", "");
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("spring.output.ansi.enabled", "never");
        
        // Explicitly disable web server
        System.setProperty("spring.main.web-application-type", "NONE");
        
        // Set non-interactive mode property if stdin is not available
        // This will be used by the JsonRpcStdioServer to adjust its behavior
        if (System.console() != null) {
            System.setProperty("jsonrpc.stdio.interactive", "true");
        } else {
            System.setProperty("jsonrpc.stdio.interactive", "false");
            System.err.println("WARNING: Running in non-interactive mode. " +
                              "The server may not receive input if stdin is not properly connected.");
        }
        
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