package com.bookshop.order.order.domain;

import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("orders")
public record Order(
        @Id
        Long id,
        String bookIsbn,
        String bookName,
        double orderTotal,
        int quantity,
        OrderStatus status,
        @CreatedDate
        Instant createdDate,
        @LastModifiedDate
        Instant lastModifiedDate,
        @CreatedBy
        String createdBy,
        @LastModifiedBy
        String lastModifiedBy,
        @Version
        int version,
        String reason) {
    public static Order of(String bookIsbn, String bookName, double orderTotal, int quantity, OrderStatus status, String reason) {
        return new Order(null, bookIsbn, bookName, orderTotal, quantity, status, null, null, null, null, 0, reason);
    }
}
