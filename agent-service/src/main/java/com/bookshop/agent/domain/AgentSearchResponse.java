package com.bookshop.agent.domain;

import java.util.List;

public record AgentSearchResponse(
        String message,
        List<BookSummary> books
) {
    public AgentSearchResponse {
        books = books == null
                ? List.of()
                : List.copyOf(books);
    }
}
