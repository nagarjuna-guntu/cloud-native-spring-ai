package com.bookshop.edge.web;

import com.bookshop.edge.ordersummary.OrderSummary;
import com.bookshop.edge.ordersummary.OrderSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders/summary")
@Slf4j
public class OrderSummaryController {
    private final OrderSummaryService orderSummaryService;

    public OrderSummaryController(OrderSummaryService orderSummaryService) {
        this.orderSummaryService = orderSummaryService;
    }

    @GetMapping("{ID}")
    public Mono<OrderSummary> viewOrderSummary(@PathVariable Long ID) {
        return orderSummaryService.viewOrderSummary(ID);
    }

    @GetMapping
    public Flux<OrderSummary> viewAllOrderSummaries() {
        log.info("viewAllOrderSummaries endpoint calling..");
        return orderSummaryService.viewAllOrderSummariesByUser();
    }
}
