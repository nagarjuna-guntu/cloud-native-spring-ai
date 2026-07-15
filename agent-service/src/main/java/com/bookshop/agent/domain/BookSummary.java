package com.bookshop.agent.domain;

public record BookSummary(
        String isbn,
        String title,
        String author,
        String publisher,
        double price
) {
}
