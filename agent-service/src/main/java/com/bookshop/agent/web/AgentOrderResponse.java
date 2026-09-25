package com.bookshop.agent.web;


import com.bookshop.agent.domain.OrderSummary;

public record AgentOrderResponse(
        String message,
        OrderSummary orderSummary) {
}
