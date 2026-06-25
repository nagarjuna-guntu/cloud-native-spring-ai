package com.bookshop.dispatch.event;

import java.time.Instant;

public record OrderAccepted(Long orderId, Instant acceptedDate) {}

