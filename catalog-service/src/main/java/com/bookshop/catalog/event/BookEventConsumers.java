package com.bookshop.catalog.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class BookEventConsumers {
    private static final String BOOKS_BY_ISBN = "booksByIsbn";
    private static final String BOOKS = "books";

    private final CacheManager cacheManager;
    private final BookEventMapper bookEventMapper;

    public BookEventConsumers(CacheManager cacheManager, BookEventMapper bookEventMapper) {
        this.cacheManager = cacheManager;
        this.bookEventMapper = bookEventMapper;
    }

    @Bean
    public Consumer<BookCreatedEvent> bookCreatedConsumer() {
        return event -> {
            log.info("Book created event consumed with ISBN: {}", event.isbn());
            var bookResponse = bookEventMapper.mapToBookResponse(event);
            ifCachePresent(BOOKS_BY_ISBN, cache ->  cache.put(bookResponse.isbn(), bookResponse));
            ifCachePresent(BOOKS, Cache::clear); // clear the existing list cache and rebuild when @Cacheable on getAll methods
        };
    }

    @Bean
    public Consumer<BookUpdatedEvent> bookUpdatedConsumer() {
        return event -> {
            log.info("Book updated event consumed with ISBN: {}", event.isbn());
            var bookResponse = bookEventMapper.mapToBookResponse(event);
            ifCachePresent(BOOKS_BY_ISBN, cache ->  cache.put(bookResponse.isbn(), bookResponse));
            ifCachePresent(BOOKS, Cache::clear);
        };
    }

    private void ifCachePresent(String cacheName, Consumer<Cache> action) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            action.accept(cache);
        } else {
            log.warn("Cache '{}' not configured. Skipping cache operation.", cacheName);
        }
    }
}
