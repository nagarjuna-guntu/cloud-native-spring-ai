package com.bookshop.catalog.demo;


import com.bookshop.catalog.domain.Book;
import com.bookshop.catalog.domain.BookRepository;
import com.bookshop.catalog.domain.Publisher;
import com.bookshop.catalog.web.BookMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
public class BookDataLoader {

    private final BookRepository bookRepository;
    private final CacheManager cacheManager;
    private final BookMapper bookMapper;
    private final VectorStore vectorStore;
    private final JdbcClient jdbcClient;

    public BookDataLoader(BookRepository bookRepository, CacheManager cacheManager, BookMapper bookMapper,
                          VectorStore vectorStore, JdbcClient jdbcClient) {
        this.bookRepository = bookRepository;
        this.cacheManager = cacheManager;
        this.bookMapper = bookMapper;
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        bookRepository.deleteAll();
        jdbcClient.sql("DELETE FROM vector_store")
                .update();

        var book1 = Book.of(
                "1491910771",
                "Head First Java: A Brain-Friendly Guide",
                "Kathy Sierra",
                9.90, Publisher.O_Reilly.getName()
        );
        var book2 = Book.of(
                "0134685997",
                "Effective Java 3rd Edition",
                "Joshua Bloch",
                59.99, Publisher.Addison_Wesley.getName());

        var book3 = Book.of(
                "0135404541",
                "Core Java for the Impatient 4th Edition",
                "Cay Horstmann",
                71.99, Publisher.Addison_Wesley.getName());
        var book4 = Book.of(
                "1617294543",
                "Microservices Patterns: With examples in Java",
                "Chris Richardson",
                59.12, Publisher.Manning.getName());
        var book5 = Book.of(
                "1492076988",
                "Spring Boot: Up and Running: Building Cloud Native Java and Kotlin Applications",
                "Mark Heckler",
                65.12, Publisher.O_Reilly.getName());
        var book6 = Book.of(
                "1633437973",
                "Spring Security in Action, Second Edition",
                "Laurentiu Spilca",
                47.4, Publisher.Manning.getName());

        var books = List.of(book1, book2, book3, book4, book5, book6);
        var savedBooks = bookRepository.saveAll(books);
        preloadBookCaches(savedBooks);
        preloadVectorStore(savedBooks);

    }

    private void preloadVectorStore(List<Book> savedBooks) {
        log.info("Preloading vector store with books count {}", savedBooks.size());
        var documents = savedBooks.stream()
                .map(this::toDocument)
                .toList();

        // Spring AI automatically duplicates your metadata map onto every split chunk.
        vectorStore.add(documents);
        log.info("Vector store add completed with {} documents", documents.size());
    }

    private Document toDocument(Book book) {
        log.info("Converting book {}", book);
        String stableId = UUID.nameUUIDFromBytes(book.isbn().getBytes()).toString();
        String content = String.format("Title: %s. Author: %s. Publisher: %s. Price: %.2f",
                book.title(), book.author(), book.publisher(), book.price());

        Map<String, Object> metadata = Map.of(
                "isbn", book.isbn(),
                "price", book.price(),
                "title", book.title().toLowerCase(),
                "author", book.author().toLowerCase(),
                "publisher", book.publisher().toLowerCase()
        );

        return new Document(stableId, content, metadata);
    }

    private void preloadBookCaches(List<Book> books) {
        var bookResponses = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
        Cache byIsbnCache = cacheManager.getCache("booksByIsbn");
        Cache allBooksCache = cacheManager.getCache("books");

        if (byIsbnCache == null || allBooksCache == null) {
            return;
        }

        bookResponses.forEach(book -> byIsbnCache.put(book.isbn(), book));
        allBooksCache.put("ALL", bookResponses);
    }
}
