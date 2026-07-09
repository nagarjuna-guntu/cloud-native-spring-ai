package com.bookshop.agent;


public record AgentOrderResponse(
        String message,
        OrderSummary orderSummary

) {
}
