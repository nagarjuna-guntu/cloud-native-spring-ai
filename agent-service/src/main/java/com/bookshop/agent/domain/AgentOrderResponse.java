package com.bookshop.agent.domain;


public record AgentOrderResponse(
        String message,
        OrderSummary orderSummary) { }
