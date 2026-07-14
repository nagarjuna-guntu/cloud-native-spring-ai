package com.bookshop.order.order.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record OrderRequest(

        @NotBlank(message = "The book ISBN must be defined.")
        @McpToolParam(description = "The exact string ISBN of the book being purchased. Required.", required = true)
        String isbn,

        @NotNull(message = "The book quantity must be defined.")
        @McpToolParam(description = "The number of items to order (between 1 and 5). Defaults to 1 if missing.", required = false)
        @Min(value = 1, message = "You must order at least 1 item.")
        @Max(value = 5, message = "You cannot order more than 5 items.")
        int quantity
) {
}
