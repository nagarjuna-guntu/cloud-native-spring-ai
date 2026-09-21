package com.bookshop.catalog.domain;


import com.bookshop.catalog.event.BookEventPublisher;
import com.bookshop.catalog.event.BookEventType;
import com.bookshop.catalog.web.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookEventPublisher bookEventPublisher;
    private final BookValidator bookValidator;
    private final VectorStoreService vectorStoreService;


    public BookService(BookRepository bookRepository,
                       BookMapper bookMapper,
                       BookEventPublisher bookEventPublisher,
                       BookValidator bookValidator,
                       VectorStoreService vectorStoreService) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.bookEventPublisher = bookEventPublisher;
        this.bookValidator = bookValidator;
        this.vectorStoreService = vectorStoreService;
    }

    @Cacheable(cacheNames = "books", key = "'ALL'", sync = true)
    public List<BookResponse> viewBooks() {
        var books = bookRepository.findAll();
        return books.stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    // sync = true avoid concurrent cache misses, only one request get the data from DB
    // other requests are waiting for the cache to populate and gets the data from cache
    // instead every parallel request calls DB.
    @Cacheable(cacheNames = "booksByIsbn", key = "#isbn", sync = true)
    public BookResponse viewBookDetails(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() ->
                        new BookNotFoundException("The Book with ISBN %s was not found".formatted(isbn)));
    }

    @Transactional
    public BookResponse addBook(CreateBookRequest bookRequest) {
        return switch (bookRepository.existsByIsbn(bookRequest.isbn())) {
            case false -> saveAndPublishBookEvents(bookMapper.toEntity(bookRequest), BookEventType.BOOK_CREATED);
            case true ->
                    throw new BookAlreadyExistsException("The Book with ISBN %s already exists".formatted(bookRequest.isbn()));
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
                        saveAndPublishBookEvents(bookMapper.toEntity(existingBook, bookRequest),
                                BookEventType.BOOK_UPDATED))
                .orElseThrow(() -> new BookNotFoundException("The Book with ISBN %s was not found".formatted(isbn)));
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
                .orElseThrow(() -> new BookNotFoundException("The Book with ISBN %s not found".formatted(isbn)));
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
        log.info("Getting books by ISBNs: {}", isbns);
        return bookRepository.findAllByIsbnIn(isbns).stream()
                .map(bookMapper::toBookResponse)
                .toList();
    }

    public List<BookResponse> search(String query) {
        log.info("Searching for books with query: {}", query);
        try {
            // Phase 1: Vector Space Search (Might throw VectorStoreException)
            List<Document> documents = vectorStoreService.vectorStoreSearch(query);
            log.info("Books Search results Count- [{}] ", documents.size());
            if (documents.isEmpty()) {
                return List.of();
            }
            List<String> isbns = documents.stream()
                    .map(this::extractIsbn)
                    .flatMap(Optional::stream)
                    .distinct()
                    .toList();

            if (isbns.isEmpty()) {
                log.warn("Vector search returned {} document(s), but no valid ISBN metadata was found", documents.size());
                return List.of();
            }

            return getBooksByIsbns(isbns);
            // use pattern patching switch expression to handle each exception types
        } catch (Throwable cause) {
            throw switch (cause) {
                case VectorStoreException vectorStoreException -> {
                    log.error("Vector store search failed: {}", vectorStoreException.getMessage(), vectorStoreException);
                    yield new BookSearchException("Failed to search for books due to vector store error", vectorStoreException);
                }
                case DataAccessException dataAccessException -> {
                    log.error("Database access error during book search: {}", dataAccessException.getMessage(), dataAccessException);
                    yield new BookSearchException("Failed to search for books due to database access error", dataAccessException);
                }
                case Throwable throwable -> {
                    log.error("Unexpected error during book search: {}", throwable.getMessage(), throwable);
                    yield new BookSearchException("Failed to search for books due to an unexpected error", throwable);
                }
            };
        }
    }

    private Optional<String> extractIsbn(Document document) {
        var isbn = document.getMetadata().get("isbn");
        if (isbn == null) {
            return Optional.empty();
        }
        var value = isbn.toString().strip();
        return value.isEmpty()
                ? Optional.empty()
                : Optional.of(value);
    }
}
