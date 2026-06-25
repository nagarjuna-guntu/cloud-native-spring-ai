package com.bookshop.edge.ordersummary;

import java.time.Instant;

public record OrderSummary(
        Long orderId, String bookIsbn, String bookTitle, double bookPrice,
        String bookAuthor, String bookPublisher, int quantity,
        double orderTotal, String orderStatus, Instant createdDate,
        String createdBy, String rejectReason) {

    public static OrderSummary of(Book book, Order order) {
        return new OrderSummary(
                order.id(), book.isbn(),
                book.title(), book.price(),
                book.author(), book.publisher(),
                order.quantity(), order.orderTotal(),
                order.status(), order.createdDate(),
                order.createdBy(), order.reason());
    }

}
