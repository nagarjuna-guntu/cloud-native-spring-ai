package com.bookshop.order.order.event;

import java.time.Instant;

public record OrderAccepted(Long orderId, Instant acceptedDate) {
}
