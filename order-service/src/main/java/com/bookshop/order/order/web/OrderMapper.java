package com.bookshop.order.order.web;


import com.bookshop.order.book.Book;
import com.bookshop.order.order.domain.Order;
import com.bookshop.order.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class OrderMapper {

    public Order toAcceptedOrder(Book book, int quantity) {
        var joinedBookTitle = String.join(
                "_",
                book.title(),
                book.author(),
                book.publisher()
        );

        return Order.of(
                book.isbn(),
                joinedBookTitle,
                book.price() * quantity,
                quantity,
                OrderStatus.ACCEPTED,
                ""
        );
    }

    public Order toDispatchedOrder(Order existingOrder, Instant orderDispatchedDate) {
        return new Order(existingOrder.id(), existingOrder.bookIsbn(),
                existingOrder.bookName(), existingOrder.orderTotal(),
                existingOrder.quantity(), OrderStatus.DISPATCHED,
                existingOrder.createdDate(), orderDispatchedDate,
                existingOrder.createdBy(), existingOrder.lastModifiedBy(),
                existingOrder.version(), existingOrder.reason()
        );
    }

    public Order toRejectedOrder(String isbn, int quantity, String reason) {
        return Order.of(
                isbn,
                null, 0,
                quantity,
                OrderStatus.REJECTED,
                reason);
    }

    public OrderResponse toOrderResponse(Order entity) {
        return new OrderResponse(
                entity.id(),
                entity.bookIsbn(),
                entity.bookName(),
                entity.orderTotal(),
                entity.quantity(),
                entity.status(),
                entity.createdDate(),
                entity.createdBy(),
                entity.reason()
        );
    }
}
