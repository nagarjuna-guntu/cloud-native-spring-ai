package com.bookshop.catalog.tools;

import com.bookshop.catalog.web.BookResponse;

import java.util.List;

public record BookSearchResult(List<BookResponse> books) {
    public BookSearchResult {
        books = books == null
                ? List.of()
                : List.copyOf(books);
    }

    public static BookSearchResult of(List<BookResponse> books) {
        return new BookSearchResult(books);
    }
}
