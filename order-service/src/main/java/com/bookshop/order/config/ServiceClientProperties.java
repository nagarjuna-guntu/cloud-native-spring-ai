package com.bookshop.order.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "services")
public record ServiceClientProperties(@NotNull CatalogProps catalog) {
        public record CatalogProps(@NotNull String baseUrl) {}
        public String catalogServiceUrl() {return catalog.baseUrl();}
}
