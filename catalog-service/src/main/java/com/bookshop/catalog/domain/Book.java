package com.bookshop.catalog.domain;

import lombok.Builder;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder(toBuilder = true)
@Table("book")
public record Book(

        @Id
        Long id,
        String isbn,
        String title,
        String author,
        Double price,
        String publisher,

        @CreatedDate
        @Column("create_date")
        Instant createdDate,

        @LastModifiedDate
        @Column("last_modified_date")
        Instant lastModifiedDate,

        @CreatedBy
        String createdBy,

        @LastModifiedBy
        String lastModifiedBy,

        @Version
        int version
) {

    public static Book of(String isbn, String title, String author, double price, String publisher) {
        return new Book(null, isbn, title, author, price, publisher, null, null, null, null, 0);
    }
}
