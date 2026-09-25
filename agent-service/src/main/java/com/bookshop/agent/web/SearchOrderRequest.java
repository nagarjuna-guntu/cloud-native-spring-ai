package com.bookshop.agent.web;

import jakarta.validation.constraints.NotNull;

public record SearchOrderRequest(
        @NotNull
        Long orderId
) {
}
