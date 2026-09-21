package com.bookshop.edge.config;

public final class CachedKeys {
    private static final String BOOKS_CACHE_PREFIX = "book-catalog::";
    private static final String BOOK_BY_ISBN_KEY_PREFIX = "booksByIsbn::";
    private static final String BOOKS_KEY_PREFIX = "books::";

    public static String bookByIsbn(String isbn) {
        return BOOKS_CACHE_PREFIX + BOOK_BY_ISBN_KEY_PREFIX + isbn;
    }

    public static String booksALL(String key) {

        return BOOKS_CACHE_PREFIX + BOOKS_KEY_PREFIX + key;
    }
}
