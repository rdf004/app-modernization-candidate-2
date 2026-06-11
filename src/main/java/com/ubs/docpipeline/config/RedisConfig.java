package com.ubs.docpipeline.config;

import org.springframework.beans.factory.annotation
    .Value;
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

@Configuration
public class RedisConfig {

    @Value("${docpipeline.redis.host:"
        + "settle-cache-01.internal}")
    private String redisHost;

    @Value("${docpipeline.redis.port:6379}")
    private int redisPort;

    @Value("${docpipeline.redis.database:2}")
    private int redisDb;

    @Bean
    public JedisConnectionFactory
            redisConnFactory() {
        RedisStandaloneConfiguration cfg =
            new RedisStandaloneConfiguration();
        cfg.setHostName(redisHost);
        cfg.setPort(redisPort);
        cfg.setDatabase(redisDb);
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
