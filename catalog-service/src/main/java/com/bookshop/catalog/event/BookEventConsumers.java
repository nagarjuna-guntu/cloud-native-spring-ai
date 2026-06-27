package com.bookshop.catalog.event;

import com.bookshop.catalog.web.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class BookEventConsumers {
    private static final String BOOKS_BY_ISBN = "booksByIsbn";
    private static final String BOOKS = "books";

    private final CacheManager cacheManager;
    private final BookEventMapper bookEventMapper;
    private final VectorStore vectorStore;

    public BookEventConsumers(CacheManager cacheManager, BookEventMapper bookEventMapper, VectorStore vectorStore) {
        this.cacheManager = cacheManager;
        this.bookEventMapper = bookEventMapper;
        this.vectorStore = vectorStore;
    }

    @Bean
    public Consumer<BookCreatedEvent> bookCreatedConsumer() {
        return event -> {
            log.info("Book created event consumed with ISBN: {}", event.isbn());
            var bookResponse = bookEventMapper.mapToBookResponse(event);
            updateCache(bookResponse);
            updateVectorIndex(bookResponse); // <--- 2. Sync to AI
        };
    }

    private void updateVectorIndex(BookResponse bookResponse) {
        try {
            // A. Generate a Stable UUID from the ISBN
            // This ensures that "ISBN-123" ALWAYS equals UUID "abc-123..."
            // allowing us to overwrite the old vector when the book updates.
            String stableId = UUID.nameUUIDFromBytes(bookResponse.isbn().getBytes()).toString();

            // B. Construct the Context
            String content = String.format("Title: %s. Author: %s. Publisher: %s. Price: %.2f",
                    bookResponse.title(), bookResponse.author(), bookResponse.publisher(), bookResponse.price());
            // C. Metadata (Store ISBN for reverse lookup)
            Map<String, Object> map = Map.of(
                    "isbn", bookResponse.isbn(),
                    "price", bookResponse.price()
            );
            // D. Create Document with the STABLE ID
            Document document = new Document(stableId, content, map);

            // Add the documents to PGVector (Spring AI removes the old ID and inserts the new one)
            vectorStore.add(List.of(document));
            log.info("Book with ISBN: {} added to vector index successfully.", bookResponse.isbn());
        } catch (Exception e) {
            log.error("Failed to add book with ISBN: {} to vector index. Error: {}", bookResponse.isbn(), e.getMessage(), e);
        }
    }

    private void updateCache(BookResponse bookResponse) {
        ifCachePresent(BOOKS_BY_ISBN, cache ->  cache.put(bookResponse.isbn(), bookResponse));
        ifCachePresent(BOOKS, Cache::clear); // clear the existing list cache and rebuild when @Cacheable on getAll methods
    }

    @Bean
    public Consumer<BookUpdatedEvent> bookUpdatedConsumer() {
        return event -> {
            log.info("Book updated event consumed with ISBN: {}", event.isbn());
            var bookResponse = bookEventMapper.mapToBookResponse(event);
            updateCache(bookResponse);
            updateVectorIndex(bookResponse);
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
