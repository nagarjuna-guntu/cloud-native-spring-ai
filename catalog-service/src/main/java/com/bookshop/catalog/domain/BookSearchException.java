package com.bookshop.catalog.domain;

public class BookSearchException extends RuntimeException {
    
    public BookSearchException(String message) {
        super(message);
    }

    public BookSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
