package com.github.freeacs.config;

import com.github.freeacs.common.hikari.DatabaseConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource getDataSource(Environment env) throws URISyntaxException {
        return DatabaseConfig.builder()
                .databaseUrl(env.getProperty("DATABASE_URL"))
                .jdbcUrl(env.getProperty("main.datasource.jdbcUrl"))
                .driverClassName(env.getProperty("main.datasource.driverClassName"))
                .username(env.getProperty("main.datasource.username"))
                .password(env.getProperty("main.datasource.password"))
                .minimumIdle(env.getProperty("main.datasource.minimum-idle", Integer.class))
                .maximumPoolSize(env.getProperty("main.datasource.maximum-pool-size", Integer.class))
                .poolName(env.getProperty("main.datasource.poolName"))
                .build()
                .getDataSource();
    }
}
