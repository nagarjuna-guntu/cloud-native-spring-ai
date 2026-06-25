package com.bookshop.order.book;

import lombok.Builder;

@Builder(toBuilder = true)
public record Book(
        String isbn,
        String title,
        String author,
        double price,
        String publisher) {
}
