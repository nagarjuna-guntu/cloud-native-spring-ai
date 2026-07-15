package com.bookshop.order.order.event;

import java.time.Instant;

public record OrderAcceptedEvent(
        Long orderId,
        Instant occurredOn
) implements OrderEvent {
}
