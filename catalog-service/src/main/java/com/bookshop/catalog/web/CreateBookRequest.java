package com.bookshop.catalog.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateBookRequest(
        @NotBlank(
                message = "The book ISBN must be defined."
        )
        @Pattern(
                regexp = "^([0-9]{10}|[0-9]{13})$",
                message = "The ISBN format must be valid."
        )
        String isbn,
        @NotBlank(
                message = "The book title must be defined."
        )
        String title,
        @NotBlank(
                message = "The book author must be defined."
        )
        String author,
        double price,
        String publisher
) {
}
