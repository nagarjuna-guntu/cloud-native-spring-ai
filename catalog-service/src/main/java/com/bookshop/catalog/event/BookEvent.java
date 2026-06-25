package com.bookshop.catalog.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface BookEvent permits BookCreatedEvent, BookUpdatedEvent {
    UUID eventId();
    Instant occurredOn();
}
