package com.bookshop.catalog.tools;

import com.bookshop.catalog.domain.BookService;
import com.bookshop.catalog.web.BookResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;


import java.util.Collections;
import java.util.List;

@Component
public class CatalogTools {
    private final BookService bookService;
    private final VectorStore  vectorStore;

    public CatalogTools(BookService bookService, VectorStore vectorStore) {
        this.bookService = bookService;
        this.vectorStore = vectorStore;
    }

    @McpTool(name = "lookupBooks",
            description = """
                    This tool allows you to search for books in the catalog based on a query.
                    The query can be a book title, author, or any relevant keyword.
                    The tool returns a list of books that match the query.
                    Each book in the list includes its ISBN, title, author, price, and publisher.
                    """)
    public List<BookResponse> lookupBooks(@ToolParam String query) {
        List<Document> documents = searchVectorstore(query);
        if (documents.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> isbns = documents.stream()
                .map(doc -> (String) doc.getMetadata().get("isbn"))
                .toList();

        return bookService.getBooksByIsbns(isbns);
    }

    private List<Document> searchVectorstore(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .similarityThreshold(0.7)
                .topK(5)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}
