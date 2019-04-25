package com.github.freeacs.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource getDataSource(Environment config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(config.getProperty("main.datasource.driverClassName"));
        hikariConfig.setJdbcUrl(config.getProperty("main.datasource.jdbcUrl"));
        hikariConfig.setUsername(config.getProperty("main.datasource.username"));
        hikariConfig.setPassword(config.getProperty("main.datasource.password"));

        hikariConfig.setMinimumIdle(config.getProperty("main.datasource.minimum-idle", Integer.class, 1));
        hikariConfig.setMaximumPoolSize(config.getProperty("main.datasource.maximum-pool-size", Integer.class, 10));
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName(config.getProperty("main.datasource.poolName"));

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        hikariConfig.setAutoCommit(true);

        return new HikariDataSource(hikariConfig);
    }
}
