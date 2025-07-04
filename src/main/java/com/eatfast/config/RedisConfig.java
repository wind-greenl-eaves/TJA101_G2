// src/main/java/com/eatfast/config/RedisConfig.java
package com.eatfast.config; // 假設在 config 包下

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory; // 引入 Redis 連線工廠
import org.springframework.data.redis.core.RedisTemplate; // 引入 RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer; // 引入 Jackson 序列化器
import org.springframework.data.redis.serializer.StringRedisSerializer; // 引入字串序列化器

@Configuration // 標記這個類別是一個 Spring 配置類別
public class RedisConfig {

    @Bean // 標記這個方法會產生一個 Spring Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 【可自定義名稱】: redisTemplate 方法名稱
        // 【不可變動的關鍵字/語法】: public, @Bean, RedisTemplate, RedisConnectionFactory 等
        // 說明: 這個方法會創建並配置一個 RedisTemplate 實例，然後將它作為一個 Bean 註冊到 Spring 容器中。
        // RedisConnectionFactory 會由 Spring Boot 自動配置好並注入進來。

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory); // 將自動配置好的連線工廠設定給 RedisTemplate

        // Key 的序列化器：決定 Redis 中 Key 的存儲格式
        // 【不可變動的關鍵字/語法】: StringRedisSerializer
        // 說明: 使用 StringRedisSerializer，將 Java 中的 String Key 直接以 UTF-8 字串形式存儲在 Redis 中，可讀性高。
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer()); // Hash 結構的 Key (Field) 也使用 String 序列化器

        // Value 的序列化器：決定 Redis 中 Value 的存儲格式
        // 【不可變動的關鍵字/語法】: GenericJackson2JsonRedisSerializer
        // 說明: 使用 GenericJackson2JsonRedisSerializer，它會將 Java 物件自動序列化為 JSON 字串存儲在 Redis 中。
        // 這允許您直接將複雜的 Java 物件（例如您的 CartItemRedisData DTO）作為值存入 Redis，非常方便。
        // 反序列化時也會自動將 JSON 字串轉回 Java 物件。
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // Hash 結構的 Value 也使用 JSON 序列化器

        template.afterPropertiesSet(); // 初始化 RedisTemplate 的所有屬性
        return template; // 將配置好的 RedisTemplate Bean 返回給 Spring 容器
    }
}