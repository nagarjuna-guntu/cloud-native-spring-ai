package com.bookshop.dispatch.event;


import java.time.Instant;

public record OrderDispatchedEvent(
        Long orderId,
        Instant occurredOn) implements OrderEvent {
}
