package com.bookshop.agent.web;

import com.bookshop.agent.domain.OrderAgentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/order")
@Slf4j
public class OrderAgentController {

    private final OrderAgentService orderAgentService;

    public OrderAgentController(OrderAgentService orderAgentService) {
        this.orderAgentService = orderAgentService;
    }

    @PostMapping
    public AgentOrderResponse placeOrder(@RequestHeader(name = "X_AI_CHAT_ID", defaultValue = "default") String chatId,
                                         @RequestBody @Valid CreateOrderRequest request) {
        log.info("Place Order X_AI_CHAT_ID = {}", chatId);
        return orderAgentService.placeOrder(request, chatId);
    }

    @PostMapping("/search")
    public AgentOrderResponse findOrder(@RequestHeader(name = "X_AI_CHAT_ID", defaultValue = "default") String chatId,
                                        @RequestBody @Valid SearchOrderRequest request) {
        log.info("Find Order X_AI_CHAT_ID = {}, Order ID = {}", chatId, request);
        return orderAgentService.findOrder(request, chatId);
    }
}
