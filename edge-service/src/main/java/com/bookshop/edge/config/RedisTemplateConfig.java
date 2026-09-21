package com.bookshop.edge.config;


import com.bookshop.edge.ordersummary.Book;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Configuration
public class RedisTemplateConfig {

    @Bean
    public ReactiveRedisTemplate<String, List<Book>> booksRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
                                                                        RedisSerializer<List<Book>> redisBooksValueSerializer) {
        RedisSerializationContext<String, List<Book>> serializationContext = RedisSerializationContext
                .<String, List<Book>>newSerializationContext(new StringRedisSerializer())
                .value(redisBooksValueSerializer)
                .hashValue(redisBooksValueSerializer)
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public ReactiveRedisTemplate<String, Book> booksByIsbnRedisTemplate(ReactiveRedisConnectionFactory connectionFactory,
                                                                        RedisSerializer<Book> redisBookByIsbnValueSerializer) {
        RedisSerializationContext<String, Book> serializationContext = RedisSerializationContext
                .<String, Book>newSerializationContext(new StringRedisSerializer())
                .value(redisBookByIsbnValueSerializer)
                .hashValue(redisBookByIsbnValueSerializer)
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public RedisSerializer<List<Book>> redisBooksValueSerializer() {
        var jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        JavaType listType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, Book.class);
        return new JacksonJsonRedisSerializer<>(jsonMapper, listType);
    }

    @Bean
    public RedisSerializer<Book> redisBookByIsbnValueSerializer() {
        var jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        return new JacksonJsonRedisSerializer<>(jsonMapper, Book.class);
    }
}
