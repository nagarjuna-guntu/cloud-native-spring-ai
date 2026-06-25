package com.bookshop.edge.clients;


import com.bookshop.edge.config.ServiceClientConfigProperties;
import com.bookshop.edge.ordersummary.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class OrderClient {
    private final WebClient webClient;
    private final ServiceClientConfigProperties clientProperties;


    public OrderClient(WebClient.Builder builder, ServiceClientConfigProperties clientProperties) {
        this.webClient = builder.baseUrl(clientProperties.orderServiceUrl()).build();
        this.clientProperties = clientProperties;

    }


    public Mono<Order> getOrder(Long id) {
        log.info("Fetching order by id {} ", id);
        return webClient.get()
                .uri("/orders/{id}", id)
                .retrieve()
                .bodyToMono(Order.class)
                .timeout(Duration.ofSeconds(1));
    }


    public Flux<Order> getAllOrdersByUser() {
        log.info("Fetching order by logged in user..." );
        return webClient.get()
                .uri("/orders")
                .retrieve()
                .bodyToFlux(Order.class)
                .timeout(Duration.ofSeconds(1));
    }
}
