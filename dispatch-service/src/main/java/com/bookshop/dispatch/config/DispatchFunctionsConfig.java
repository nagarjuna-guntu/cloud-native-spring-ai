package com.bookshop.dispatch.config;


import com.bookshop.dispatch.event.OrderAcceptedEvent;
import com.bookshop.dispatch.event.OrderDispatchedEvent;
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
    public Function<OrderAcceptedEvent, Long> pack() {
        return orderAccepted -> {
            Instant packedTimestamp = Instant.now();
            log.info("The order with id {} is packed at [{}]",
                    orderAccepted.orderId(), packedTimestamp);
            return orderAccepted.orderId();
        };
    }

    //Order Labeling Function
    @Bean
    public Function<Long, OrderDispatchedEvent> label() {
        return orderId -> {
            Instant dispatchTimestamp = Instant.now();
            log.info("The order with id {} is labeled at [{}]",
                    orderId, dispatchTimestamp);
            return new OrderDispatchedEvent(orderId, dispatchTimestamp);
        };
    }
}
