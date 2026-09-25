package com.bookshop.catalog.demo;


import com.bookshop.catalog.domain.*;
import com.bookshop.catalog.web.BookMapper;
import com.bookshop.catalog.web.BookResponse;
import com.bookshop.catalog.web.IsbnValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Objects;


@Slf4j
public class BookDataLoader {

    private final BookRepository bookRepository;
    private final CacheManagerService cacheManagerService;
    private final BookMapper bookMapper;
    private final VectorStoreService vectorStoreService;
    private final JdbcClient jdbcClient;
    private final IsbnValidator isbnValidator;

    public BookDataLoader(BookRepository bookRepository, CacheManagerService cacheManagerService, BookMapper bookMapper,
                          VectorStoreService vectorStoreService, JdbcClient jdbcClient, IsbnValidator isbnValidator) {
        this.bookRepository = bookRepository;
        this.cacheManagerService = cacheManagerService;
        this.bookMapper = bookMapper;
        this.vectorStoreService = vectorStoreService;
        this.jdbcClient = jdbcClient;
        this.isbnValidator = isbnValidator;
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
        List<BookResponse> savedBooks = preloadDatabase(books);
        preloadCaches(savedBooks);
        preloadVectorStore(savedBooks);

    }

    private List<BookResponse> preloadDatabase(List<Book> books) {
        var validBooks = books.stream()
                .filter(this::validateBook)
                .toList();
        var savedBooks = bookRepository.saveAll(validBooks);
        return savedBooks.stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    private boolean validateBook(Book book) {
        return Objects.nonNull(book) && isbnValidator.isIsbnCandidate(book.isbn());
    }

    private void preloadVectorStore(List<BookResponse> savedBooks) {
        log.info("Preloading vector store with books count {}", savedBooks.size());
        vectorStoreService.vectorStoreAdd(savedBooks);
    }

    private void preloadCaches(List<BookResponse> savedBooks) {
        log.info("Preloading book caches with books count {}", savedBooks.size());
        savedBooks.forEach(cacheManagerService::cacheAdd);
        cacheManagerService.cacheAll(savedBooks);
    }
}
