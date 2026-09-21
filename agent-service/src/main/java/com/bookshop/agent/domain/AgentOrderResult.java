package com.bookshop.agent.domain;

import java.time.Instant;

public record AgentOrderResult(
        Long id,
        String bookIsbn,
        String bookName,
        Double orderTotal,
        Integer quantity,
        OrderStatus status,
        Instant createdDate,
        String createdBy,
        String reason
) { }
