package com.bookshop.agent;

public record BookSummary(
        String isbn,
        String title,
        String author,
        double price
) {
}
