package com.bookshop.edge.ordersummary;


import com.bookshop.edge.clients.OrderServiceClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final OrderServiceClient orderServiceClient;

    public OrderService(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }


    public Mono<Order> findOrderById(Long orderId) {
        return orderServiceClient.findOrderById(orderId);
    }


    public Flux<Order> getAllOrdersByUser() {
        return orderServiceClient.getAllOrdersByUser();
    }
}
