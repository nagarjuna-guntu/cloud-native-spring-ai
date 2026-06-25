package com.bookshop.catalog.demo;


import com.bookshop.catalog.domain.Book;
import com.bookshop.catalog.domain.BookRepository;
import com.bookshop.catalog.domain.Publisher;
import com.bookshop.catalog.web.BookMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;

import java.util.List;

public class BookDataLoader {
    private final BookRepository bookRepository;
    private final CacheManager cacheManager;
    private final BookMapper bookMapper;

    public BookDataLoader(BookRepository bookRepository, CacheManager cacheManager, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.cacheManager = cacheManager;
        this.bookMapper = bookMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() {
        bookRepository.deleteAll();
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
