package com.bookshop.agent.domain;

import com.bookshop.agent.config.AgentMcpTools;
import com.bookshop.agent.config.AgentPromptTemplates;
import com.bookshop.agent.web.AgentOrderResponse;
import com.bookshop.agent.web.CreateOrderRequest;
import com.bookshop.agent.web.SearchOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
public class OrderAgentService {

    private final ChatClient chatClient;
    private final AgentMcpTools mcpTools;
    private final MessageChatMemoryAdvisor chatMemoryAdvisor;
    private final AgentPromptTemplates promptTemplates;
    private final JsonMapper jsonMapper;

    public OrderAgentService(ChatClient chatClient, AgentMcpTools mcpTools, MessageChatMemoryAdvisor chatMemoryAdvisor, AgentPromptTemplates promptTemplates, JsonMapper jsonMapper) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
        this.chatMemoryAdvisor = chatMemoryAdvisor;
        this.promptTemplates = promptTemplates;
        this.jsonMapper = jsonMapper;
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

    public AgentOrderResponse placeOrder(CreateOrderRequest agentRequest, String chatId) {

        log.info("PlaceOrder agent workflow for order request [{}] : chatId: [{}] - START", agentRequest, chatId);

        AgentOrderResult result = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplates.get("placeOrder"))
                        .param("isbn", agentRequest.isbn())
                        .param("quantity", agentRequest.quantity())
                )
                .tools(mcpTools.placeOrder())
                .call()
                .entity(AgentOrderResult.class, ChatClient.EntityParamSpec::validateSchema);

        var message = getMessage(result);
        OrderSummary summary = OrderSummary.of(result);

        log.info("PlaceOrder agent workflow for order request [{}] : chatId: [{}] - RETURNED", agentRequest, chatId);

        return new AgentOrderResponse(message, summary);
    }

    public AgentOrderResponse findOrder(SearchOrderRequest request, String chatId) {

        log.info("FindOrder agent workflow for order id [{}] - chatId: [{}] - START", request.orderId(), chatId);
        //Map<String, Object> promptArguments = Map.of("orderId", request.orderId());

        AgentOrderResult result = chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text(promptTemplates.get("findOrder"))
                        .param("orderId", request.orderId())
                )
                .advisors(advisorSpec -> advisorSpec
                        .advisors(chatMemoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, chatId)
                )
                .tools(mcpTools.findOrder())
                .call()
                .entity(AgentOrderResult.class, ChatClient.EntityParamSpec::validateSchema);

        var message = getMessage(result);
        OrderSummary summary = OrderSummary.of(result);

        log.info("FindOrder agent workflow for order id [{}] - chatId: [{}] - RETURNED", request.orderId(), chatId);

        return new AgentOrderResponse(message, summary);
    }
}
