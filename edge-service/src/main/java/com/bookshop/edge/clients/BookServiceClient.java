package com.bookshop.edge.clients;


import com.bookshop.edge.ordersummary.Book;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/books")
public interface BookServiceClient {

    @GetExchange("{ISBN}")
    Mono<Book> findBookByIsbn(@PathVariable String ISBN);
}
