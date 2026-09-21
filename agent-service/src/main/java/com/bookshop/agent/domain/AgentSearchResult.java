package com.bookshop.agent.domain;

import java.util.List;

public record AgentSearchResult(List<BookSummary> books) {
    public AgentSearchResult {
        books = books == null
                ? List.of()
                : List.copyOf(books);
    }
}
