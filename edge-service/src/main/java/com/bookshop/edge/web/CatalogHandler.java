package com.bookshop.edge.web;


import com.bookshop.edge.config.CachedKeys;
import com.bookshop.edge.ordersummary.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@Slf4j
public class CatalogHandler {

    private final ReactiveRedisTemplate<String, List<Book>> booksRedisTemplate;
    private final ReactiveRedisTemplate<String, Book> booksByIsbnRedisTemplate;

    public CatalogHandler(ReactiveRedisTemplate<String, List<Book>> booksRedisTemplate, ReactiveRedisTemplate<String, Book> booksByIsbnRedisTemplate) {
        this.booksRedisTemplate = booksRedisTemplate;
        this.booksByIsbnRedisTemplate = booksByIsbnRedisTemplate;
    }

    public Mono<ServerResponse> getFallback(ServerRequest request) {
        log.info("CatalogHandler::getFallback is calling ...");
        var isbn = request.pathVariables().getOrDefault("ISBN", "");
        return isbn.isBlank() || isbn.equals("/") ? getFallbackBooks() : getFallbackBooksByIsbn(isbn);
    }

    public Mono<ServerResponse> getFallbackBooks() {
        log.info("CatalogHandler::getFallbackBooks is calling ...");
        var cacheKeyBooksAll = CachedKeys.booksALL("ALL");
        log.info("Fetching data from the Cache for the cache key {}", cacheKeyBooksAll);
        return booksRedisTemplate.opsForValue()
                .get(cacheKeyBooksAll)
                .flatMap(books -> ok().bodyValue(books))
                // Handles "Key Not Found" or "Cache Not Initialized"
                .switchIfEmpty(handleCacheMiss(cacheKeyBooksAll))
                // Handles Redis Connection Failures
                .onErrorResume(this::handleCacheError);

    }

    public Mono<ServerResponse> getFallbackBooksByIsbn(String ISBN) {
        log.info("CatalogHandler::getFallbackByIsbn is calling for the book with ISBN : {}...", ISBN);
        var cleanIsbn  = ISBN.startsWith("/") ? ISBN.substring(1) : ISBN;
        var cacheKeyBooksByIsbn = CachedKeys.bookByIsbn(cleanIsbn);
        log.info("Fetching data from the Cache for the key {}", cacheKeyBooksByIsbn);
        return booksByIsbnRedisTemplate.opsForValue()
                .get(cacheKeyBooksByIsbn)
                .flatMap(books -> ok().bodyValue(books))
                // Handles "Key Not Found" or "Cache Not Initialized"
                .switchIfEmpty(handleCacheMiss(cacheKeyBooksByIsbn))
                // Handles Redis Connection Failures
                .onErrorResume(e -> handleCacheError(e, cleanIsbn));

    }

    public Mono<ServerResponse> postFallback(ServerRequest serverRequest) {
       return errorHandler(serverRequest);
    }

    public Mono<ServerResponse> errorHandler(ServerRequest serverRequest) {
        //Spring Cloud Gateway stores the exception that caused the fallback
        Throwable cause = serverRequest.exchange().getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        return switch (cause) {
            case null ->
                    ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body(Mono.just("service not available"), String.class);
            case Throwable ex -> //log exception or save exception to db here
                    ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(Mono.just("service not available due to " + ex.getMessage()), String.class);

        };
    }

    private Mono<ServerResponse> handleCacheError(Throwable ex, String... isbn) {
        log.error("Redis error for ISBN {}: {}", isbn, ex.getMessage());
        // Fallback to a message or service instead of crashing
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyValue("Cache connection failed." + ex.getMessage());
    }

    private Mono<ServerResponse> handleCacheMiss(String cacheKey) {
        log.info("Cache miss for the cache key {}", cacheKey);
        // Logic for when the key is simply not in Redis
        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .bodyValue("Cache miss for the cache key " + cacheKey);

    }
}
