package com.bookshop.catalog.tools;

import com.bookshop.catalog.domain.BookService;
import com.bookshop.catalog.web.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
                    Searches the book catalog using a natural-language query.
                    
                    Use this tool to find books by title, part title, author,
                    publisher or relevant keywords.
                    
                    The tool returns matching books with ISBN, title, author,
                    price, and publisher.
                    An empty result means no matching books were found.
                    """,
            generateOutputSchema = true,
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            ))
    public List<BookResponse> searchBook(@McpToolParam(
            description = "Natural-language book search query, such as title or part title, author, or any relevant keyword",
            required = true) String query) {

        log.info("MCP tool called: searchBook with query: {}", query);

        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        return bookService.search(query);
    }

}
