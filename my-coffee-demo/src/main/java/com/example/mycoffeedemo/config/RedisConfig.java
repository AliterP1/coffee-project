package com.example.mycoffeedemo.config;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置连接工厂
        redisTemplate.setConnectionFactory(factory);

        // 设置Key序列化器
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        // 设置Value序列化器（可选择不同的序列化方式）
        // 1. JDK序列化（默认，但可读性差）
        redisTemplate.setValueSerializer(RedisSerializer.java());
        redisTemplate.setHashValueSerializer(RedisSerializer.java());

        // 2. JSON序列化（推荐，可读性好）
        ObjectMapper om = new ObjectMapper();
        // 设置可见性，允许访问所有字段
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 开启类型信息存储（解决反序列化时的类型识别问题）
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        // 直接通过构造函数传入ObjectMapper（推荐方式）
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(om, Object.class);
        // 设置Value序列化器
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        // 初始化参数设置
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
