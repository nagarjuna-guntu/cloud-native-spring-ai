package com.bookshop.order.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "services")
public record ServiceClientProperties(@NotNull CatalogProps catalog) {
    public String catalogServiceUrl() {
        return catalog.baseUrl();
    }

    public record CatalogProps(@NotNull String baseUrl) {
    }
}
