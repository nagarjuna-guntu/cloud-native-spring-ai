package com.bookshop.catalog.tools;

import com.bookshop.catalog.domain.BookService;
import com.bookshop.catalog.web.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CatalogTools {
    private final BookService bookService;

    public CatalogTools(BookService bookService) {
        this.bookService = bookService;
    }

    @McpTool(name = "searchBook",
            description = """
                    This tool allows you to search for books in the catalog based on a query.
                    The query can be a book title or part title, author, or any relevant keyword.
                    The tool returns a list of books that match the query.
                    Each book in the list includes its ISBN, title, author, price, and publisher.
                    """)
    public List<BookResponse> searchBook(@McpToolParam(
            description = "The input query can be a book title or part title, author, or any relevant keyword",
            required = true) String query) {
        log.info("searchBook({})", query);
        return bookService.search(query);
    }

}
