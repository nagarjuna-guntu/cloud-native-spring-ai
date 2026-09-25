package com.bookshop.catalog.domain;

import com.bookshop.catalog.config.VectorSearchProperties;
import com.bookshop.catalog.web.BookResponse;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collection;

import java.util.List;

@Service
@Slf4j
public class VectorStoreService {

    private static final String SEARCH_ENGINE = "pgvector";
    private static final String RETRIEVAL_MODE = "retrieval_mode";
    private static final String RELAXED_RETRIEVAL = "relaxed";

    private final VectorStore vectorStore;
    private final DocumentMapper documentMapper;
    private final ObservationRegistry observationRegistry;
    private final VectorSearchProperties vectorSearchProperties;

    public VectorStoreService(VectorStore vectorStore, DocumentMapper documentMapper, ObservationRegistry observationRegistry, VectorSearchProperties vectorSearchProperties) {
        this.vectorStore = vectorStore;
        this.documentMapper = documentMapper;
        this.observationRegistry = observationRegistry;
        this.vectorSearchProperties = vectorSearchProperties;
    }

    /**
     * Performs semantic search against the vector store.
     *
     * <p>The search follows two stages:
     *
     * <ol>
     *     <li>Strict similarity search</li>
     *     <li>Optional relaxed similarity search when strict search returns no results</li>
     * </ol>
     *
     * <p>An empty result is a valid business outcome and is not treated as an error.
     *
     * @param query natural-language search query
     * @return matching vector documents, or an empty list when there are no matches
     * @throws VectorStoreException when the vector store or embedding infrastructure fails
     */
    public List<Document> vectorStoreSearch(String query) {
        log.debug("Searching vector store for query: [{}].", query);
        if (!StringUtils.hasText(query)) {
            log.debug("Aborting vector store search; input query is empty or blank.");
            return List.of();
        }
        var normalizeQuery = query.trim();
        return Observation.createNotStarted("catalog.vector.search", observationRegistry).lowCardinalityKeyValue("search.engine", SEARCH_ENGINE).highCardinalityKeyValue("search.query", normalizeQuery).observe(() -> executeSearch(normalizeQuery));
    }

    private List<Document> executeSearch(String query) {
        log.debug("Executing primary strict vector search for query: [{}]", query);

        try {
            List<Document> strictResults = executeSimilaritySearch(query, vectorSearchProperties.strict().topK(), vectorSearchProperties.strict().similarityThreshold());

            if (!strictResults.isEmpty()) {
                log.debug("Strict vector search returned [{}] result(s)", strictResults.size());
                return strictResults;
            }

            if (!vectorSearchProperties.relaxed().enabled()) {
                log.debug("Strict vector search returned no results; relaxed search is disabled");
                return List.of();
            }

            log.debug("Strict vector search returned no results; executing relaxed search");

            List<Document> relaxedResults = executeSimilaritySearch(query, vectorSearchProperties.relaxed().topK(), vectorSearchProperties.relaxed().similarityThreshold());

            markRelaxedResults(relaxedResults);

            log.debug("Relaxed vector search returned [{}] result(s)", relaxedResults.size());
            return relaxedResults;
        } catch (Exception e) {
            throw handleExceptions("SEARCH: '" + query + "'", e);
        }
    }

    private void markRelaxedResults(List<Document> documents) {
        documents.forEach(document -> document.getMetadata().put(RETRIEVAL_MODE, RELAXED_RETRIEVAL));
    }

    private List<Document> executeSimilaritySearch(String query, int topK, double similarityThreshold) {
        var searchRequest = SearchRequest.builder().query(query).topK(topK).similarityThreshold(similarityThreshold).build();
        return vectorStore.similaritySearch(searchRequest);
    }

    public void vectorStoreAdd(BookResponse bookResponse) {

        if (!isValidBook(bookResponse)) {
            log.warn("Refusing to add null or invalid book record to vector store.");
            return;
        }

        var isbn = bookResponse.isbn().trim();
        Observation.createNotStarted("catalog.vector.add.single", observationRegistry).lowCardinalityKeyValue("operation.mode", "single_upsert").highCardinalityKeyValue("book.isbn", bookResponse.isbn()).observe(() -> addSingleDocument(bookResponse, isbn));
    }

    private void addSingleDocument(BookResponse book, String isbn) {

        try {
            Document document = documentMapper.toDocument(book);
            vectorStore.add(List.of(document));
            log.debug("Adding book with ISBN: [{}] to vector store - successfully.", isbn);
        } catch (Exception e) {
            throw handleExceptions("ADD SINGLE: " + isbn, e);
        }
    }

    public void vectorStoreAdd(Collection<BookResponse> bookResponses) {
        log.debug("Adding [{}] books to vector store.", bookResponses.size());
        if (bookResponses.isEmpty()) {
            return;
        }
        Observation.createNotStarted("catalog.vector.add.batch", observationRegistry).lowCardinalityKeyValue("operation.mode", "batch_upsert").highCardinalityKeyValue("batch.payload.size", String.valueOf(bookResponses.size())).observe(() -> addBatch(bookResponses));
    }

    private void addBatch(Collection<BookResponse> books) {

        try {
            List<Document> documents = books.stream().filter(this::isValidBook).map(documentMapper::toDocument).toList();

            if (documents.isEmpty()) {
                log.debug("Vector batch insert skipped because no valid documents were produced");
                return;
            }

            vectorStore.add(documents);
            log.info("Adding books [{}] to vector store - successfully.", documents.size());
        } catch (Exception e) {
            throw handleExceptions("ADD BATCH: " + books.size(), e);
        }
    }

    private boolean isValidBook(BookResponse book) {
        return book != null && StringUtils.hasText(book.isbn());
    }

    private VectorStoreException handleExceptions(String context, Exception e) {

        throw switch (e) {
            // Catches situation where Ollama responds with an error code (e.g., model missing or invalid payload)
            case RestClientResponseException apiEx -> {
                log.error("Ollama HTTP API returned error status [{}] during [{}].", apiEx.getStatusCode(), context, apiEx);
                yield new VectorStoreException("Embedding inference server returned an invalid response", apiEx);
            }
            // Catches connection timeouts, container outages, or drops to the local Ollama engine
            case ResourceAccessException networkEx -> {
                log.error("Network timeout or connection refused for Ollama server during [{}].", context, networkEx);
                yield new VectorStoreException("AI Embedding engine is currently unreachable", networkEx);
            }
            // Catches PostgreSQL pgvector connection pooling exhaustion or index issues
            case DataAccessException dbEx -> {
                log.error("PostgreSQL pgvector storage failure encountered during [{}]", context, dbEx);
                yield new VectorStoreException("Vector storage data store failure encountered", dbEx);
            }
            case Exception ex -> {
                log.error("Unexpected operational pipeline failure encountered during [{}].", context, ex);
                yield new VectorStoreException("Internal vector retrieval processing error occurred", ex);
            }
        };
    }
}



