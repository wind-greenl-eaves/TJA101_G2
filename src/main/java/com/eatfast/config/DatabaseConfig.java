package com.eatfast.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * DatabaseConfig
 *
 * 這個配置類用於資料庫相關的設定。
 *
 * - @Configuration 註解表示這個類是 Spring Bean 定義的來源。
 * - 如有需要，可以在這裡加入自訂的資料庫設定。
 * - 目前這個類別允許應用程式即使資料庫連線失敗也能啟動。
 *
 * 用法：
 *   根據專案需求，加入特定的 DataSource 或 JPA 設定。
 */
@Configuration
public class DatabaseConfig {
    
    // 此配置允許應用程式即使資料庫連線失敗也能啟動
    // 如有需要，可以在這裡加入特定的資料庫設定
}