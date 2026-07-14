package com.bookshop.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class BookAgentService {


    private static final String SEARCH_BOOK_SYSTEM_PROMPT = """
            You are a strict data-fetching assistant.
            
            CRITICAL SAFETY RULES:
            1. You MUST execute the 'searchBook' tool to fetch data.
            2. If the tool returns NO records, an empty list [], an empty string, or an error,
               your output response MUST be exactly: "No data found".
            3. DO NOT use your internal knowledge, do not invent books, do not guess ISBNs, 
               and do not make up any response text if the tool returns nothing.
          
            """;

    private static final String PLACE_ORDER_SYSTEM_PROMPT = """
            You are a dedicated Book Ordering Assistant.
            Your ONLY job is to process book purchases using the order system.
            
            CRITICAL RULES:
            1. You MUST call the 'placeOrder' tool immediately to execute the purchase.
            2. Do not attempt to search for books or perform any other actions.
            """;

    private static final String SEARCH_BOOK_PROMPT = """
            You must look up the book using the catalog system.
            CRITICAL RULE:
            1. Do not use your own knowledge or make up book information.
            2. First, call the 'searchBook' tool with the keyword: {keyword}.
            3. Once you receive the tool response data, extract the books and format them.

            populate AgentSearchResponse as follows:
            response:
            A friendly sentence describing the search result.
            books:
            Copy every returned book into the books list.
            Do not invent books.
            Do not invent ISBNs.
            If the tool returns no books,
            return
            response:
            "No books found."
            books:
            []
            """;

    private static final String PLACE_ORDER_PROMPT = """
            You must order the book using the order system.
            CRITICAL RULE:
            1. Do not use your internal knowledge base or make up book information.
            2. Place an order for ISBN: '{isbn}' with a quantity of {quantity}"
            3. Once you receive the tool response data, extract the order information and format them.

            """;

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpTools;

    public BookAgentService(ChatClient chatClient, SyncMcpToolCallbackProvider mcpTools) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
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
                .system(PLACE_ORDER_SYSTEM_PROMPT)
                .user(promptUserSpec -> promptUserSpec
                        .text(PLACE_ORDER_PROMPT)
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
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("Transform the following text receipt into the required structure: {data}")
                        .param("data", placeOrderResponse)
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
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
                .system(SEARCH_BOOK_SYSTEM_PROMPT)
                .user(promptUserSpec -> promptUserSpec
                        .text(SEARCH_BOOK_PROMPT)
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

        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("""
                                Transform the following factual search details directly into the schema.
                                If the data contains a refusal or says no books exist, output 'No data found.'
                                books: [] as the response.
                    
                                Data to map: {data}
                                """
                        )
                        .param("data", searchBookResult)
                )
                .advisors(advisorSpec -> advisorSpec
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .advisors(validationAdvisor)
                .call()
                .entity(AgentSearchResponse.class, entityParamSpec -> entityParamSpec
                        .useProviderStructuredOutput()
                        .validateSchema());

    }
}
