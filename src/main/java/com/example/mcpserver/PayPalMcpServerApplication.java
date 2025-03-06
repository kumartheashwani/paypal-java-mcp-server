package com.example.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.Banner;

@SpringBootApplication
public class PayPalMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PayPalMcpServerApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
} 