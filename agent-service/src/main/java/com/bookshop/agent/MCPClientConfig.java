package com.bookshop.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MCPClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        ToolCallback[] toolCallbacks = mcpToolCallbackProvider.getToolCallbacks();
        var validationAdvisor = StructuredOutputValidationAdvisor
                .builder()
                .maxRepeatAttempts(2)
                .outputType(ChatResponse.class)
                .build();
        return chatClientBuilder
                .defaultSystem("""
                        You are a helpful Bookstore Concierge.
                        "Use the catalog to find books and the order tool to buy them.
                        "If the user doesn't specify quantity, assume 1.""")
                .defaultTools(toolCallbacks)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build(), validationAdvisor)
                .build();
    }
}
