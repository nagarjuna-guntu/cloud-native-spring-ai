package com.bookshop.catalog.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class BookSearchService {
    private final VectorStore vectorStore;

    public BookSearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> searchVectorstore(String query) {
        log.info("searchVectorstore({})", query);
        var normalizeQuery = query.trim().toLowerCase();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(normalizeQuery)
                .topK(5)
                .similarityThreshold(0.3)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }
}
