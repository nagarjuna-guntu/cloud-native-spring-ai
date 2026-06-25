package com.bookshop.edge.clients;


import com.bookshop.edge.ordersummary.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@HttpExchange("/orders")
public interface OrderServiceClient {

    @GetExchange("{orderId}")
    Mono<Order> findOrderById(@PathVariable Long orderId);

    @GetExchange
    Flux<Order> getAllOrdersByUser();
}
