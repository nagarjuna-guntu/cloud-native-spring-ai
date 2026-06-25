package com.bookshop.dispatch.event;

import java.time.Instant;

public record OrderDispatched(Long orderId, Instant dispatchedDate) {
}
