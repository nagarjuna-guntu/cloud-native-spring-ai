package com.bookshop.catalog.domain;

import com.bookshop.catalog.web.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
public class CacheManagerService {
    private static final String CACHE_BOOKS_BY_ISBN = "booksByIsbn";
    private static final String CACHE_BOOKS = "books";
    private static final String CACHE_KEY_ALL_BOOKS = "ALL";

    private final CacheManager cacheManager;

    public CacheManagerService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void cacheUpdate(BookResponse bookResponse) {
        log.debug("Updating cache for ISBN: {}", bookResponse.isbn());
        executeIfCacheExists(CACHE_BOOKS_BY_ISBN, cache -> cache.put(bookResponse.isbn(), bookResponse));
        executeIfCacheExists(CACHE_BOOKS, Cache::clear); // clear the existing list cache and rebuild when @Cacheable on getAll methods
    }

    public void cacheAdd(BookResponse bookResponse) {
        log.debug("Adding new book to cache with ISBN: {}", bookResponse.isbn());
        executeIfCacheExists(CACHE_BOOKS_BY_ISBN, cache -> cache.put(bookResponse.isbn(), bookResponse));
    }

    public void cacheAll(List<BookResponse> bookResponses) {
        log.debug("Caching all books collection");
        executeIfCacheExists(CACHE_BOOKS, cache -> cache.put(CACHE_KEY_ALL_BOOKS, bookResponses));
    }

    private void executeIfCacheExists(String cacheName, Consumer<Cache> action) {
        log.debug("Checking if cache '{}' exists.", cacheName);
        switch (cacheManager.getCache(cacheName)) {
            case Cache cache -> action.accept(cache);
            case null -> log.warn("Cache '{}' not configured. Skipping cache operation.", cacheName);
        }
    }
}
