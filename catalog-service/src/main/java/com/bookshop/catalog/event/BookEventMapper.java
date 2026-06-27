package com.bookshop.catalog.event;


import com.bookshop.catalog.web.BookResponse;
import org.springframework.stereotype.Component;

@Component
public class BookEventMapper {

    public BookResponse mapToBookResponse(BookEvent event) {
        return switch (event) {
            case BookCreatedEvent(_, _, var isbn, var title, var author, var price, var publisher) ->
                    toBookResponse(isbn, title, author, price, publisher);
            case BookUpdatedEvent(_, _, var isbn, var title, var author, var price, var publisher) ->
                    toBookResponse(isbn, title, author, price, publisher);
        };
    }

    private BookResponse toBookResponse(String isbn, String title, String author, Double price, String publisher) {
        return new BookResponse(
                isbn,
                title,
                author,
                price,
                publisher
        );
    }

}
