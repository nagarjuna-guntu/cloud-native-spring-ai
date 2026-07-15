package com.bookshop.agent.domain;

import java.time.Instant;

public record OrderSummary(
        Long orderId, String bookIsbn, String bookTitle, double bookPrice,
        String bookAuthor, String bookPublisher, int quantity,
        double orderTotal, String orderStatus, Instant createdDate,
        String rejectReason
) {
}
