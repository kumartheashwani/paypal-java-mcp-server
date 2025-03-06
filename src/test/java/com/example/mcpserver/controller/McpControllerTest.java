package com.example.mcpserver.controller;

import com.example.mcpserver.model.McpRequest;
import com.example.mcpserver.model.McpResponse;
import com.example.mcpserver.service.McpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(McpController.class)
public class McpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private McpService mcpService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void healthCheckShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("MCP Server is running"));
    }

    @Test
    public void completionsShouldReturnResponse() throws Exception {
        // Prepare test data
        McpRequest request = new McpRequest();
        request.setQuery("Test query");
        request.setMessages(new ArrayList<>());
        request.setContext(new HashMap<>());
        request.setOptions(new HashMap<>());

        McpResponse response = McpResponse.builder()
                .content("Test response")
                .toolCalls(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        // Mock service
        when(mcpService.processRequest(any(McpRequest.class))).thenReturn(response);

        // Perform request and verify
        mockMvc.perform(post("/api/v1/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }
} 