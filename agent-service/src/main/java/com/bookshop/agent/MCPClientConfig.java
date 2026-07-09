package com.bookshop.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.regex.RegexToolIndex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class MCPClientConfig {

    private static final String SYSTEM_PROMPT = """
            You are a book-ordering assistant.
            IMPORTANT:
            For search book should call searchBook tool,
            for order book should call placeOrder tool.
   
            """;

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, SyncMcpToolCallbackProvider mcpTools) {

        var toolSearchAdvisor  = ToolSearchToolCallingAdvisor.builder()
                .toolIndex(new RegexToolIndex())
                .build();

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(mcpTools)
                .defaultAdvisors(
                        messageChatMemoryAdvisor(chatMemory),
                        toolSearchAdvisor,
                        SimpleLoggerAdvisor.builder().build()
                )
                .build();
    }

    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .build();
    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(50) // default is 20
                .build();
    }
}
