package com.bookshop.agent.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;


public record AgentSearchRequest(

        @NotEmpty(message = "Search keyword can not be null or Empty.")
        @NotBlank(message = "Search keyword can not be Blank.")
        String keyword
) {
}
