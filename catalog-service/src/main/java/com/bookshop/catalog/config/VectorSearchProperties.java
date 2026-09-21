package com.bookshop.catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog.vector-search")
public record VectorSearchProperties(
        Strict strict,
        Relaxed relaxed
) {
    public record Strict(
            int topK,
            double similarityThreshold) {
    }

    public record Relaxed(
            boolean enabled,
            int topK,
            double similarityThreshold) {
    }
}
