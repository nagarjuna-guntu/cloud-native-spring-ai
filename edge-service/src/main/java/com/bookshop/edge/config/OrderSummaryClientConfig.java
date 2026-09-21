package com.bookshop.edge.config;


import com.bookshop.edge.clients.BookServiceClient;
import com.bookshop.edge.clients.OrderServiceClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@EnableConfigurationProperties(ServiceClientConfigProperties.class)
@ImportHttpServices(group = "order-service", types = OrderServiceClient.class, clientType = HttpServiceGroup.ClientType.WEB_CLIENT)
@ImportHttpServices(group = "catalog-service", types = BookServiceClient.class, clientType = HttpServiceGroup.ClientType.WEB_CLIENT)

public class OrderSummaryClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClientHttpServiceGroupConfigurer httpServiceGroupConfigurer(
            ServiceClientConfigProperties serviceClientProperties) {

        return groups -> {
            groups.filterByName("order-service")
                    .forEachClient((_, clientBuilder) -> {
                        clientBuilder.baseUrl(serviceClientProperties.orderServiceUrl());
                    });
            groups.filterByName("catalog-service")
                    .forEachClient((_, clientBuilder) -> {
                        clientBuilder.baseUrl(serviceClientProperties.catalogServiceUrl());
                    });
        };
    }
}
