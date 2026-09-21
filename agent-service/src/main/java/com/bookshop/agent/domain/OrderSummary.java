package com.bookshop.agent.domain;

import java.time.Instant;

public record OrderSummary(
        Long orderId, String bookIsbn, String bookTitle,
        int quantity, double orderTotal, String orderStatus,
        String createdBy, Instant createdDate, String rejectReason
) {
    public static OrderSummary of(AgentOrderResult order) {
        return new OrderSummary(
                order.id(), order.bookIsbn(), order.bookName(),
                order.quantity(), order.orderTotal(), order.status().name(),
                order.createdBy(), order.createdDate(), order.reason()
        );
    }
}
