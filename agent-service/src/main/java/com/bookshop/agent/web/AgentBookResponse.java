package com.bookshop.agent.web;

import com.bookshop.agent.domain.BookSummary;

import java.util.List;

public record AgentBookResponse(
        String message,
        List<BookSummary> books
) {
    public AgentBookResponse {
        books = books == null
                ? List.of()
                : List.copyOf(books);
    }
}
