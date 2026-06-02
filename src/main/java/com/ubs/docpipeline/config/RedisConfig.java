package com.ubs.docpipeline.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation
    .Configuration;
import org.springframework.data.redis.connection
    .RedisStandaloneConfiguration;
import org.springframework.data.redis.connection
    .jedis.JedisConnectionFactory;
import org.springframework.data.redis.core
    .RedisTemplate;
import org.springframework.data.redis.serializer
    .StringRedisSerializer;
import org.springframework.data.redis.serializer
    .GenericJackson2JsonRedisSerializer;

/**
 * Redis configuration for intermediate
 * processing state cache. Connects to on-prem
 * Redis at settle-cache-01.internal:6379.
 * Sticky sessions required — instances are
 * pinned to this specific host.
 */
@Configuration
public class RedisConfig {

    private static final String REDIS_HOST =
        "settle-cache-01.internal";
    private static final int REDIS_PORT = 6379;
    private static final int REDIS_DB = 2;

    @Bean
    public JedisConnectionFactory redisConnFactory() {
        RedisStandaloneConfiguration cfg =
            new RedisStandaloneConfiguration();
        cfg.setHostName(REDIS_HOST);
        cfg.setPort(REDIS_PORT);
        cfg.setDatabase(REDIS_DB);
        return new JedisConnectionFactory(cfg);
    }

    @Bean
    public RedisTemplate<String, Object>
            redisTemplate() {
        RedisTemplate<String, Object> tpl =
            new RedisTemplate<>();
        tpl.setConnectionFactory(
            redisConnFactory()
        );
        tpl.setKeySerializer(
            new StringRedisSerializer()
        );
        tpl.setValueSerializer(
            new GenericJackson2JsonRedisSerializer()
        );
        return tpl;
    }
}
