package com.bookshop.catalog.web;


import com.bookshop.catalog.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    /**
     * CREATE
     * DTO → Entity
     */
    public Book toEntity(CreateBookRequest request) {
        return Book.builder()
                .isbn(request.isbn())
                .title(request.title())
                .author(request.author())
                .price(request.price())
                .publisher(request.publisher())
                .build();
    }

    /**
     * UPDATE
     * Merge request into existing entity (preserve id, version, audit fields)
     */
    public Book toUpdatedEntity(Book existing, UpdateBookRequest request) {
        return existing.toBuilder()
                .title(request.title())
                .author(request.author())
                .price(request.price())
                .publisher(request.publisher())
                .build();
    }

    /**
     * READ
     * Entity → Response DTO
     */
    public BookResponse toBookResponse(Book entity) {
        return new BookResponse(
                entity.isbn(),
                entity.title(),
                entity.author(),
                entity.price(),
                entity.publisher()
        );
    }



}
