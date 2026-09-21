package com.bookshop.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

import java.util.Optional;

@Configuration
@EnableJdbcAuditing
public class DataAuditConfig {

    @Bean
    public AuditorAware<String> getCurrentAuditor() {
        return () -> Optional.of("Anonymous");
    }
}
