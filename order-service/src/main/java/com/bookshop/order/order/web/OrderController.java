package com.bookshop.order.order.web;


import com.bookshop.order.order.domain.OrderService;
import com.bookshop.order.order.domain.OrderStatus;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("orders")
@Slf4j
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping("{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        log.info("Fetching order by id {} ..", id);
        return orderService.findOrderById(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching orders ..");
        return orderService.findAll();
    }

    @GetMapping("/status")
    public Map<OrderStatus, List<OrderResponse>> getOrdersByStatus() {
        return orderService.findOrdersByStatus();
    }

    @PostMapping
    public OrderResponse createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        log.info("Create order with request {}..", orderRequest);
        return orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity());

    }
}
