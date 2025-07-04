package com.eatfast.cart.model;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Redis非關聯資料庫存資料
@Configuration
public class RedisConfig {
    
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 設置序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        // key 採用 String 的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash 的 key 也採用 String 的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        
        // value 序列化：支援 JSON 和 String 兩種格式
        // 對於驗證碼等簡單字串，也能正常工作
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        
        template.afterPropertiesSet();
        
        log.info("Redis 配置完成 - 支援購物車和驗證碼功能");
        
        return template;
    }
}