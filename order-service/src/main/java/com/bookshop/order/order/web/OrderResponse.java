package com.bookshop.order.order.web;


import com.bookshop.order.order.domain.OrderStatus;

import java.time.Instant;

public record OrderResponse(
        Long id,
        String bookIsbn,
        String bookName,
        Double orderTotal,
        Integer quantity,
        OrderStatus status,
        Instant createdDate,
        String createdBy,
        String reason) {
}
