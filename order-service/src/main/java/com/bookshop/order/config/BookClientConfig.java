package com.bookshop.order.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;


@Configuration
public class BookClientConfig {

    @Bean
    public RestClient catalogRestClient(RestClient.Builder builder, ServiceClientProperties props) {
        return builder
                .baseUrl(props.catalog().baseUrl())
                .build();
    }

    @Bean
    public RestClientCustomizer  restClientCustomizer() {
        return restClientBuilder -> {
            HttpClientSettings settings = HttpClientSettings.defaults()
                    .withConnectTimeout(Duration.ofSeconds(2))
                    .withReadTimeout(Duration.ofSeconds(3));
            restClientBuilder
                    .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
                    .requestInterceptor((request, body, execution) -> {
                        return execution.execute(request, body);
                    });
        };

    }

    @Bean
    public RetryTemplate retryTemplate() {
        var retryPolicy = RetryPolicy.builder()
                .includes(java.util.concurrent.TimeoutException.class,
                        HttpServerErrorException.GatewayTimeout.class,
                        HttpServerErrorException.BadGateway.class,
                        HttpServerErrorException.ServiceUnavailable.class)
                .maxRetries(3)
                .delay(Duration.ofMillis(100))
                .multiplier(2)
                .maxDelay(Duration.ofMillis(500))
                .build();
        return new RetryTemplate(retryPolicy);

    }
}
