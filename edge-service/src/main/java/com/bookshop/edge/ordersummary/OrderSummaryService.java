package com.bookshop.edge.ordersummary;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderSummaryService {
    private final OrderService orderService;
    private final BookService bookService;

    public OrderSummaryService(OrderService orderService, BookService bookService) {
        this.orderService = orderService;
        this.bookService = bookService;
    }


    public Mono<OrderSummary> viewOrderSummary(Long id) {
        return orderService.findOrderById(id)
                .flatMap(order -> bookService.findBookByIsbn(order.bookIsbn())
                        .map(book -> OrderSummary.of(book, order)));

    }

    public Flux<OrderSummary> viewAllOrderSummariesByUser() {
        return orderService.getAllOrdersByUser()
                .flatMap(order ->
                        bookService.findBookByIsbn(order.bookIsbn())
                                .map(book -> OrderSummary.of(book, order)),
                        5 // Limit to 5 concurrent book lookups per user request
                );
    }
}
