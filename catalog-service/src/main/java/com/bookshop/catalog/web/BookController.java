package com.bookshop.catalog.web;


import com.bookshop.catalog.domain.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("books")
@Validated
@Slf4j
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Iterable<BookResponse> viewAll() {
        log.info("Fetching the list of books from catalog..");
        return bookService.viewBooks();
    }

    @GetMapping("/search")
    public List<BookResponse> search(@RequestParam(name = "query") String query) {
        log.info("Fetching the list of books from catalog by search query {}", query);
        return bookService.search(query);
    }

    @GetMapping("{ISBN}")
    public BookResponse getByIsbn(
            @PathVariable @Pattern(regexp = "^([0-9]{10}|[0-9]{13})$", message = "Invalid ISBN") String ISBN) {
        log.info("Fetching the book by ISBN {}..", ISBN);
        return bookService.viewBookDetails(ISBN);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse addBook(@Valid @RequestBody CreateBookRequest bookRequest) {
        log.info("Add book to the catalog with data {}..", bookRequest);
        return bookService.addBook(bookRequest);
    }

    @PutMapping("{ISBN}")
    public BookResponse updateBook(
            @PathVariable @Pattern(regexp = "^([0-9]{10}|[0-9]{13})$", message = "Invalid ISBN") String ISBN,
            @Valid @RequestBody UpdateBookRequest bookRequest) {
        log.info("Update the book for the ISBN {} with data {}..", ISBN, bookRequest);
        return bookService.editBook(ISBN, bookRequest);
    }

    @PatchMapping("{ISBN}")
    public BookResponse patchBook(
            @PathVariable @Pattern(regexp = "^([0-9]{10}|[0-9]{13})$", message = "Invalid ISBN") String ISBN,
            @RequestBody Map<String, Object> updates) {
        log.info("Partially update the book for the ISBN {} with data {}..", ISBN, updates);
        return bookService.editBookPartial(ISBN, updates);

    }
}
