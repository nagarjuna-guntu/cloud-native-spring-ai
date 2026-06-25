package com.bookshop.edge.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public record ServiceClientConfigProperties(
       @NotNull OrderProps order,
       @NotNull CatalogProps catalog
) {
    public record OrderProps(@NotNull String baseUrl) {}
    public record CatalogProps(@NotNull String baseUrl) {}
    public String orderServiceUrl() {return order.baseUrl();}
    public String catalogServiceUrl() {return catalog.baseUrl();}
}
