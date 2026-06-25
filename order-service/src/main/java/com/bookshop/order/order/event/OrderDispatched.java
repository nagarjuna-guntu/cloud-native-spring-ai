package com.bookshop.order.order.event;

import java.time.Instant;

public record OrderDispatched(Long orderId, Instant dispatchedDate) {
}
