package com.bookshop.order.book;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class BookClient {

    private final RestClient restClient;
    private final RetryTemplate retryTemplate;

    public BookClient(@Qualifier("catalogRestClient") RestClient restClient, RetryTemplate retryTemplate) {

        this.restClient = restClient;
        this.retryTemplate = retryTemplate;

    }


    public Book findBookByIsbn(String isbn) {
        log.info("calling findBookByIsbn with ISBN {}", isbn);
        return restClient.get()
                .uri( "/books/{ISBN}", isbn)
                .retrieve()
                .body(Book.class);
    }

    public ApiResponse<Book> getBook(String isbn) {
        try {
            return retryTemplate.execute(() -> {
                var book = findBookByIsbn(isbn);
                return new Success<>(book);
            });
        } catch (RetryException e) {
            log.error("Error in fetching book with ISBN {}, exception - {}, message - {}, exception list - {}",
                    isbn, e.getLastException(), e.getMessage(), e.getExceptions());
            return switch (e.getLastException()) {
                case HttpClientErrorException ex -> new Failure<>(ex.getStatusCode().toString(), ex);
                case HttpServerErrorException ex -> new Failure<>(ex.getStatusCode().toString(), ex);
                case Throwable ex -> new Failure<>(ex.getMessage(), ex);
            };
        }
    }
}
