package com.bookshop.dispatch.event;

import java.time.Instant;

public sealed interface OrderEvent permits OrderAcceptedEvent, OrderDispatchedEvent {
    Long orderId();

    Instant occurredOn();
}
