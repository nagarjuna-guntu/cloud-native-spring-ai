package com.bookshop.order.order.event;

import com.bookshop.order.order.domain.Order;
import com.bookshop.order.order.domain.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class OrderEventPublisher {

    private final StreamBridge streamBridge;

    public OrderEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishOrderAcceptedEvent(Order order) {
        log.info("Publishing OrderAcceptedEvent: {}", order);
        if (order.status() == OrderStatus.ACCEPTED) {
            var orderAcceptedEvent = new OrderAcceptedEvent(order.id(), Instant.now());
            log.info("Sending order accepted event with id {}", order.id());
            var isSent = streamBridge.send("orderAccepted-out-0", orderAcceptedEvent);
            log.info("Sending data for order with id {} successful ? {}", order.id(), isSent);
        }
    }
}
