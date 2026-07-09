package com.bookshop.agent;

import java.util.List;

public record AgentSearchResponse(
        String message,
        List<BookSummary> books
) {
}
