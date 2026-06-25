package com.bookshop.dispatch.config;


import com.bookshop.dispatch.event.OrderAccepted;
import com.bookshop.dispatch.event.OrderDispatched;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.function.Function;

@Configuration
@Slf4j
public class DispatchFunctionsConfig {

    //Order Packing Function
    @Bean
    public Function<OrderAccepted, Long> pack() {
        return orderAccepted -> {
            log.info("The order with id {} is packed and the order accepted date {}",
                    orderAccepted.orderId(), orderAccepted.acceptedDate());
            return orderAccepted.orderId();
        };
    }

    //Order Labeling Function
    @Bean
    public Function<Long, OrderDispatched> label() {
        return orderId -> {
            log.info("The order with id {} is labeled", orderId);
            return new OrderDispatched(orderId, Instant.now());
        };
    }
}
