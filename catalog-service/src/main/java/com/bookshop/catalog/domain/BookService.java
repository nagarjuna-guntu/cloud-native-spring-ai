package com.bookshop.catalog.domain;


import com.bookshop.catalog.event.BookEventPublisher;
import com.bookshop.catalog.event.BookEventType;
import com.bookshop.catalog.web.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookEventPublisher bookEventPublisher;
    private final BookValidator bookValidator;
    private final BookSearchService bookSearchService;


    public BookService(BookRepository bookRepository, BookMapper bookMapper, BookEventPublisher bookEventPublisher, BookValidator bookValidator, BookSearchService bookSearchService) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.bookEventPublisher = bookEventPublisher;
        this.bookValidator = bookValidator;
        this.bookSearchService = bookSearchService;
    }

    @Cacheable(cacheNames = "books", key = "'ALL'")
    public Iterable<BookResponse> viewBooks() {
        var books = bookRepository.findAll();
        return books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    @Cacheable(cacheNames = "booksByIsbn", key = "#isbn")
    public BookResponse viewBookDetails(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() ->
                        new BookNotFoundException("The Book with ISBN " + isbn + " was not found"));
    }

    @Transactional
    public BookResponse addBook(CreateBookRequest bookRequest) {
        return switch (bookRepository.existsByIsbn(bookRequest.isbn())) {
            case false -> saveAndPublishBookEvents(bookMapper.toEntity(bookRequest), BookEventType.BOOK_CREATED);
            case true ->
                    throw new BookAlreadyExistsException("The Book with ISBN " + bookRequest.isbn() + " already exists");
        };
    }

    private BookResponse saveAndPublishBookEvents(Book bookEntity, BookEventType eventType) {
        var book = bookRepository.save(bookEntity);
        bookEventPublisher.publishBookEvents(book, eventType);
        return bookMapper.toBookResponse(book);
    }

    @Transactional
    public BookResponse editBook(String isbn, UpdateBookRequest bookRequest) {
        return bookRepository.findByIsbn(isbn)
                .map(existingBook ->
                        saveAndPublishBookEvents(bookMapper.toUpdatedEntity(existingBook, bookRequest),
                                BookEventType.BOOK_UPDATED))
                .orElseThrow(() -> new BookNotFoundException("The Book with ISBN " + isbn + " was not found"));
    }

    public BookResponse editBookPartial(String isbn, Map<String, Object> updates) {
        MapBindingResult errors = new MapBindingResult(updates, "bookUpdates");
        bookValidator.validate(updates, errors);
        if (errors.hasErrors()) {
            throw new MapFieldValidationException("Field Validation Errors", errors);
        }
        //validateFields(updates);
        return bookRepository.findByIsbn(isbn)
                .map(book -> toUpdatedBook(book, updates))
                .map(bookRepository::save)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new BookNotFoundException("The Book with ISBN " + isbn + " not found"));
    }

    private Book toUpdatedBook(Book existingBook, Map<String, Object> updates) {
        Book.BookBuilder bookBuilder = existingBook.toBuilder();
        updates.forEach((key, value) -> {
            switch (key) {
                case "title" -> bookBuilder.title((String) value);
                case "author" -> bookBuilder.author((String) value);
                case "publisher" -> bookBuilder.publisher((String) value);
                case "price" -> bookBuilder.price((double) value);
            }
        });
        return bookBuilder.build();
    }

    public List<BookResponse> getBooksByIsbns(List<String> isbns) {
        log.info("getBooksByIsbns({})", isbns);
        return bookRepository.findAllByIsbnIn(isbns).stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    public List<BookResponse> search(String query) {

        log.info("search({})", query);
        List<Document> documents = bookSearchService.searchVectorstore(query);
        log.info("documents count from vectorStore - {} ", documents.size());
        if (documents.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> isbns = documents.stream()
                .map(doc -> (String) doc.getMetadata().get("isbn"))
                .toList();
        log.info("isbn metadata from vectorStore - [{}] ", isbns);

        return getBooksByIsbns(isbns);

    }
}
