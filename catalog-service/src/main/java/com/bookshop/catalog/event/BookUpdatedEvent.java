package com.bookshop.catalog.event;

import java.time.Instant;
import java.util.UUID;

public record BookUpdatedEvent(
        UUID eventId,
        Instant occurredOn,
        String isbn,
        String title,
        String author,
        Double price,
        String publisher) implements BookEvent {
}
