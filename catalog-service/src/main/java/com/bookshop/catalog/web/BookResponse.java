package com.bookshop.catalog.web;

public record BookResponse(
        String isbn,
        String title,
        String author,
        Double price,
        String publisher
) {
}
