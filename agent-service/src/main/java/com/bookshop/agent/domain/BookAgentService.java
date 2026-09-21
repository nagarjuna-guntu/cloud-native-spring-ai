package com.bookshop.agent.domain;


import com.bookshop.agent.config.AgentMcpTools;
import com.bookshop.agent.config.AgentPromptTemplates;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BookAgentService {

    private final ChatClient chatClient;
    private final AgentMcpTools mcpTools;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final AgentPromptTemplates promptTemplates;
    private final JsonMapper jsonMapper;

    public BookAgentService(ChatClient chatClient,
                            AgentMcpTools mcpTools,
                            MessageChatMemoryAdvisor chatMemoryAdvisor,
                            AgentPromptTemplates promptTemplates,
                            JsonMapper jsonMapper) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
        this.chatMemoryAdvisor = chatMemoryAdvisor;
        this.promptTemplates = promptTemplates;
        this.jsonMapper = jsonMapper;
    }

    private static String getAgentResponseText(ChatResponse chatResponse, ToolCallback toolCallback) {
        if (chatResponse == null) {
            throw new ToolExecutionException(toolCallback.getToolDefinition(),
                    new RuntimeException("tool execution returns null"));
        }
        Generation generation = chatResponse.getResult();
        return generation != null ? generation.getOutput().getText() : "";
    }

    private static @NonNull String getMessage(AgentOrderResult order) {
        return switch (order.status()) {
            case ACCEPTED -> "Order Accepted.";
            case REJECTED -> "Order Rejected.";
            case DISPATCHED -> "Order Dispatched.";
            case PENDING -> "Order Pending.";
            case CANCELED -> "Order Cancelled.";
            case null -> throw new IllegalStateException("Unexpected value: null.");
        };
    }

    public AgentOrderResponse placeOrder(AgentOrderRequest agentRequest, String chatId) {

        log.info("PlaceOrder agent workflow for order request [{}] : chatId: [{}] - START", agentRequest, chatId);

        var chatResponse = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplates.get("placeOrder"))
                        .param("isbn", agentRequest.isbn())
                        .param("quantity", agentRequest.quantity())
                )
                .tools(mcpTools.placeOrder())
                .call()
                .chatResponse();

        String responseText = getAgentResponseText(chatResponse, mcpTools.placeOrder());

        log.info("PlaceOrder tool raw output text received: [{}].", responseText);

        if (responseText == null || responseText.isBlank()) {
            return new AgentOrderResponse(responseText, null);
        }

        var result = jsonMapper.readValue(responseText, AgentOrderResult.class);

        var message = getMessage(result);
        OrderSummary summary = OrderSummary.of(result);

        log.info("PlaceOrder agent workflow for order request [{}] : chatId: [{}] - RETURNED", agentRequest, chatId);

        return new AgentOrderResponse(message, summary);
    }

    public AgentSearchResponse searchBook(AgentSearchRequest request, String chatId) {

        log.info("SearchBook agent workflow for search request [{}] : chatId: [{}] - START", request, chatId);

        AgentSearchResult agentSearchResult = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplates.get("searchBook"))
                        .param("keyword", request.keyword())
                )
                .advisors(advisorSpec -> advisorSpec
                        .advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .tools(mcpTools.searchBook())
                .call()
                .entity(AgentSearchResult.class, ChatClient.EntityParamSpec::validateSchema);

        String message = agentSearchResult.books().isEmpty()
                ? "No Book(s) Found."
                : " %d Book(s) Found.".formatted(agentSearchResult.books().size());

        log.info("SearchBook agent workflow for search request [{}] : chatId: [{}] - RETURNED", request, chatId);

        return new AgentSearchResponse(message, agentSearchResult.books());
    }

    public AgentOrderResponse findOrder(Long orderId, String chatId) {

        log.info("FindOrder agent workflow for order id [{}] - chatId: [{}] - START", orderId, chatId);
        Map<String, Object> promptArguments = Map.of("orderId", orderId);

        var chatResponse = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplates.get("findOrder"))
                        .params(promptArguments)
                )
                .advisors(advisorSpec -> advisorSpec
                        .advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .tools(mcpTools.findOrder())
                .call()
                .chatResponse();

        String responseText = getAgentResponseText(chatResponse, mcpTools.findOrder());

        log.debug("FindOrder tool raw output text received: [{}]", responseText);

        if (responseText == null || responseText.isBlank()) {
            return new AgentOrderResponse("No order found.", null);
        }

        AgentOrderResult result = jsonMapper.readValue(responseText, AgentOrderResult.class);

        var message = getMessage(result);
        OrderSummary summary = OrderSummary.of(result);

        log.info("FindOrder agent workflow for order id [{}] - chatId: [{}] - RETURNED", orderId, chatId);

        return new AgentOrderResponse(message, summary);
    }
}
