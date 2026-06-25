package com.bookshop.edge.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WebEndpoints {

    @Bean
    public RouterFunction<ServerResponse> catalogFallbackRoute(CatalogHandler catalogHandler) {
        return RouterFunctions.route()
                .GET("/catalog-fallback/{*ISBN}", catalogHandler::getFallback)
                .POST("/catalog-fallback", catalogHandler::postFallback)
                .build();
    }
}
