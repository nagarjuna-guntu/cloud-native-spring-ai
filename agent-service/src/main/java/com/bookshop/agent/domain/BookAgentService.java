package com.bookshop.agent.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class BookAgentService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpTools;
    private final Resource placeOrderSystemPrompt;
    private final Resource searchBookSystemPrompt;
    private final Resource placeOrderUserPrompt;
    private final Resource searchBookUserPrompt;

    public BookAgentService(ChatClient chatClient, SyncMcpToolCallbackProvider mcpTools,
                            @Value("classpath:/promptTemplates/placeOrderSystemPrompt.st") Resource placeOrderSystemPrompt,
                            @Value("classpath:/promptTemplates/searchBookSystemPrompt.st") Resource searchBookSystemPrompt,
                            @Value("classpath:/promptTemplates/placeOrderUserPrompt.st") Resource placeOrderUserPrompt,
                            @Value("classpath:/promptTemplates/searchBookUserPrompt.st") Resource searchBookUserPrompt) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
        this.placeOrderSystemPrompt = placeOrderSystemPrompt;
        this.searchBookSystemPrompt = searchBookSystemPrompt;
        this.placeOrderUserPrompt = placeOrderUserPrompt;
        this.searchBookUserPrompt = searchBookUserPrompt;
    }

    public AgentOrderResponse placeOrder(AgentOrderRequest agentRequest, String chatId) {
        var placeOrderTool = Arrays.stream(this.mcpTools.getToolCallbacks())
                .filter(tool -> "placeOrder".equals(tool.getToolDefinition().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("placeOrder tool not found"));

        log.info("Executing placeOrder agent workflow for chatId: {}", chatId);

        var validationAdvisor = StructuredOutputValidationAdvisor
                .builder()
                .maxRepeatAttempts(2)
                .outputType(AgentOrderResponse.class)
                .build();

        var placeOrderResponse = chatClient.prompt()
                .system(placeOrderSystemPrompt)
                .user(promptUserSpec -> promptUserSpec
                        .text(placeOrderUserPrompt)
                        .param("isbn", agentRequest.isbn())
                        .param("quantity", agentRequest.quantity())
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .tools(placeOrderTool)
                .call()
                .content();

        log.info("Order Tool raw output received: {}", placeOrderResponse);

        // STEP 2: Safe structural mapping phase
        String isolatedMappingId = "transform-" + java.util.UUID.randomUUID().toString();
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Transform the following text receipt into the required structure: {data}")
                        .param("data", placeOrderResponse)
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, isolatedMappingId)
                )
                .advisors(validationAdvisor)
                .call()
                .entity(AgentOrderResponse.class, entityParamSpec -> entityParamSpec
                        .useProviderStructuredOutput()
                        .validateSchema());

    }

    public AgentSearchResponse searchBook(AgentSearchRequest request, String chatId) {
        var searchBookTool = Arrays.stream(this.mcpTools.getToolCallbacks())
                .filter(tool -> "searchBook".equals(tool.getToolDefinition().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("searchBook tool not found"));

        log.info("Executing searchBook agent workflow for chatId: {}", chatId);

        var validationAdvisor = StructuredOutputValidationAdvisor
                .builder()
                .maxRepeatAttempts(2)
                .outputType(AgentSearchResponse.class)
                .build();

        String searchBookResult =  chatClient.prompt()
                .system(searchBookSystemPrompt)
                .user(promptUserSpec -> promptUserSpec
                        .text(searchBookUserPrompt)
                        .param("keyword", request.keyword())
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .tools(searchBookTool)
                .call()
                .content();

        log.info("Tool execution complete. Result data: {}. Now mapping to structure...", searchBookResult);

        if (searchBookResult == null || searchBookResult.isBlank() || searchBookResult.toLowerCase().contains("no data found")) {
            return new AgentSearchResponse("No data found.", List.of());
        }

        String isolatedMappingId = "transform-" + java.util.UUID.randomUUID().toString();

        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("""
                                You are a strict data transformation utility. Your ONLY task is to map the factual data
                                inside the data block below into JSON matching the schema.
                                
                                CRITICAL RULE:
                                - If the text explicitly reads "No books found" or says an error occurred, output "No data found." and an empty array.
                                - Otherwise, extract the values from the array exactly as written. Never invent data or simulate errors.
                                Data to transform:
                                 {data}
                                """
                        )
                        .param("data", searchBookResult)
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, isolatedMappingId)
                )
                .advisors(validationAdvisor)
                .call()
                .entity(AgentSearchResponse.class, entityParamSpec -> entityParamSpec
                        .useProviderStructuredOutput()
                        .validateSchema());

    }

        public AgentOrderResponse findOrder(Long orderId, String chatId) {

            var findOrderTool = Arrays.stream(this.mcpTools.getToolCallbacks())
                    .filter(tool -> "findOrder".equals(tool.getToolDefinition().name()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("findOrder tool not found"));

            log.info("Executing findOrder agent workflow for chatId: {}", chatId);

            var validationAdvisor = StructuredOutputValidationAdvisor
                    .builder()
                    .maxRepeatAttempts(2)
                    .outputType(AgentOrderResponse.class)
                    .build();

            var findOrderResponse = chatClient.prompt()
                    .system("""
                            You are a strict order-fetching assistant.
                            
                            CRITICAL SAFETY RULES:
                            1. You MUST execute the 'findOrder' tool to fetch the order data.
                            2. If the tool returns NO records, an empty list [], an empty string, or an error,
                               your output response MUST be exactly: "No data found".
                            3. DO NOT use your internal knowledge, do not invent books, do not guess ISBNs,
                               and do not make up any response text if the tool returns nothing.
                            """)
                    .user(promptUserSpec -> promptUserSpec
                            .text("""
                                    You must look up the order using the order management system.
                                    
                                    CRITICAL RULE:
                                    1. Do not use your own knowledge or make up order information.
                                    2. First, call the 'findOrder' tool with the order ID: {orderId}.
                                    3. Once you receive the tool response data, extract the order details and format them.
                                    """)
                            .param("orderId", orderId)
                    )
                    .advisors(advisorSpec -> advisorSpec
                            .param(ChatMemory.CONVERSATION_ID, chatId)
                    )
                    .tools(findOrderTool)
                    .call()
                    .content();

            log.info(" Find Order Tool raw output received: {}", findOrderResponse);

            // STEP 2: Safe structural mapping phase
            String isolatedMappingId = "transform-" + java.util.UUID.randomUUID().toString();
            return chatClient.prompt()
                    .user(userSpec -> userSpec
                            .text("Transform the following text receipt into the required structure: {data}")
                            .param("data", findOrderResponse)
                    )
                    .advisors(advisorSpec -> advisorSpec
                            .param(ChatMemory.CONVERSATION_ID, isolatedMappingId)
                    )
                    .advisors(validationAdvisor)
                    .call()
                    .entity(AgentOrderResponse.class, entityParamSpec -> entityParamSpec
                            .useProviderStructuredOutput()
                            .validateSchema());
        }
}
