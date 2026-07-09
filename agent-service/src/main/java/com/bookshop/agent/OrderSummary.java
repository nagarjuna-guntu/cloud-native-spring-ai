package com.bookshop.agent;

import java.time.Instant;

public record OrderSummary(
        String bookIsbn, String bookTitle, double bookPrice,
        String bookAuthor, String bookPublisher, int quantity,
        double orderTotal, String orderStatus, Instant createdDate,
        String rejectReason
) {
}
