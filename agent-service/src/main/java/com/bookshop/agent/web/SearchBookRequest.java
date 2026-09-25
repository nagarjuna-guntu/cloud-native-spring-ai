package com.bookshop.agent.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;


public record SearchBookRequest(

        @NotEmpty(message = "Search keyword can not be null or Empty.")
        @NotBlank(message = "Search keyword can not be Blank.")
        String keyword
) {
}
