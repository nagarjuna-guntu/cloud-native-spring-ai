package com.bookshop.edge.ordersummary;


import com.bookshop.edge.clients.BookServiceClient;
import com.bookshop.edge.config.CachedKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Service
@Slf4j
public class BookService {

    private final BookServiceClient bookServiceClient;
    private final ReactiveRedisTemplate<String, Book> redisTemplate;

    public BookService(BookServiceClient bookServiceClient, ReactiveRedisTemplate<String, Book> redisTemplate) {
        this.bookServiceClient = bookServiceClient;
        this.redisTemplate = redisTemplate;
    }


    public Mono<Book> findBookByIsbn(String isbn) {
        return bookServiceClient.findBookByIsbn(isbn);
    }

    //The Cache Key:: book-catalog::booksByIsbn::
    public Mono<Book> getCachedBook(String isbn, Throwable  ex) {
        var message = Objects.requireNonNullElse(ex.getCause().getMessage(), ex.getMessage());
        log.error("Fallback for the getBook method with the cause {} ", message);
        var cacheKeyBooksByIsbn = CachedKeys.bookByIsbn(isbn);
        return redisTemplate.opsForValue()
                .get(cacheKeyBooksByIsbn)
                .timeout(Duration.ofMillis(800))
                // CASE 1: Cache Miss (Key not found) -> Re-throw original error to Advice
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Cache Miss (Key not found) {} ", cacheKeyBooksByIsbn);
                    return Mono.error(ex);
                }))
                // CASE 2: Cache Failure (Redis down) -> Still re-throw original error to Advice
                .onErrorResume(error -> {
                    log.error("Redis error during fallback: {}", error.getMessage());
                    return Mono.error(ex);
                });
    }
}
