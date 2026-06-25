package com.bookshop.catalog.config;

import com.bookshop.catalog.web.BookResponse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, RedisSerializer<List<BookResponse>> redisBooksValueSerializer, RedisSerializer<BookResponse> redisBookByIsbnValueSerializer) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .prefixCacheNameWith("book-catalog::")
                .entryTtl(Duration.ofHours(24))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        RedisCacheConfiguration booksByIsbnConfig = defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisBookByIsbnValueSerializer))
                .entryTtl(Duration.ofMinutes(20));

        RedisCacheConfiguration booksConfig = defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisBooksValueSerializer))
                .entryTtl(Duration.ofHours(12));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("books", booksConfig)
                .withCacheConfiguration("booksByIsbn", booksByIsbnConfig)
                .build();
    }

    @Bean
    public RedisSerializer<List<BookResponse>> redisBooksValueSerializer() {
        var jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        JavaType listType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, BookResponse.class);
        return new JacksonJsonRedisSerializer<>(jsonMapper, listType);
    }

    @Bean
    public RedisSerializer<BookResponse> redisBookByIsbnValueSerializer() {
        var jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        return new JacksonJsonRedisSerializer<>(jsonMapper, BookResponse.class);
    }
}
