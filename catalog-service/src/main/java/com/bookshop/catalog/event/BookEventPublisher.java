package com.bookshop.catalog.event;


import com.bookshop.catalog.domain.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class BookEventPublisher {

    private final StreamBridge streamBridge;

    public BookEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishBookCreated(Book book) {
        BookCreatedEvent bookCreatedEvent = new BookCreatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                book.isbn(),
                book.title(),
                book.author(),
                book.price(),
                book.publisher()
        );
        log.info("Published book created event with ISBN {}", book.isbn());
        var isSent = streamBridge.send("bookCreated-out-0", bookCreatedEvent);
        log.info("Is book created event with ISBN {} successfully published ? {}", book.isbn(), isSent);
    }

    public void publishBookUpdated(Book book) {
        BookUpdatedEvent bookUpdatedEvent = new BookUpdatedEvent(
                UUID.randomUUID(),
                Instant.now(),
                book.isbn(),
                book.title(),
                book.author(),
                book.price(),
                book.publisher()
        );

        log.info("Published book updated event with ISBN {}", book.isbn());
        var isSent = streamBridge.send("bookUpdated-out-0", bookUpdatedEvent);
        log.info("Is book updated event with ISBN {} successfully published ? {}", book.isbn(), isSent);
    }

    public void publishBookEvents(Book book, BookEventType eventType) {
        switch (eventType) {
            case BOOK_CREATED -> publishBookCreated(book);
            case BOOK_UPDATED -> publishBookUpdated(book);
        }
    }
}
