package com.bookshop.order.order.event;



import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class OrderEventFunctions {

    @Bean
    public Consumer<OrderDispatchedEvent> orderDispatched(OrderEventConsumer eventConsumer) {
        return orderDispatched -> {
            eventConsumer.consumeOrderDispatchedEvent(orderDispatched);
            log.info("The order with order id {} is dispatched and dispatched date is {}",
                    orderDispatched.orderId(), orderDispatched.occurredOn());
        };

    }
}
