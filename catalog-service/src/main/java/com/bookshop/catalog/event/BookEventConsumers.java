package com.bookshop.catalog.event;

import com.bookshop.catalog.domain.CacheManagerService;
import com.bookshop.catalog.domain.VectorStoreService;
import com.bookshop.catalog.web.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class BookEventConsumers {

    private final CacheManagerService cacheManagerService;
    private final BookEventMapper bookEventMapper;
    private final VectorStoreService vectorStoreService;


    public BookEventConsumers(CacheManagerService cacheManagerService,
                              BookEventMapper bookEventMapper,
                              VectorStoreService vectorStoreService) {
        this.cacheManagerService = cacheManagerService;
        this.bookEventMapper = bookEventMapper;
        this.vectorStoreService = vectorStoreService;
    }

    @Bean
    public Consumer<BookCreatedEvent> bookCreatedConsumer() {
        return event -> {
            log.info("Book created event consumed with ISBN: {}", event.isbn());
            var bookResponse = bookEventMapper.mapToBookResponse(event);
            updateCache(bookResponse);
            updateVectorStore(bookResponse); // <--- 2. Sync to AI
        };
    }

    private void updateVectorStore(BookResponse bookResponse) {
        vectorStoreService.vectorStoreAdd(bookResponse);
    }

    private void updateCache(BookResponse bookResponse) {
        cacheManagerService.cacheUpdate(bookResponse);
    }

    @Bean
    public Consumer<BookUpdatedEvent> bookUpdatedConsumer() {
        return event -> {
            log.info("Book updated event consumed with ISBN: {}", event.isbn());
            var bookResponse = bookEventMapper.mapToBookResponse(event);
            updateCache(bookResponse);
            updateVectorStore(bookResponse);
        };
    }
}
