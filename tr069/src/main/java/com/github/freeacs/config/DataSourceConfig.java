package com.github.freeacs.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource getDataSource(Environment env) throws URISyntaxException {
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl != null) {
            URI dbUri = new URI(databaseUrl);

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbType = dbUri.getScheme().split(":")[0];
            String dbUrl = constructJdbcUrl(dbType, dbUri);

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(determineDriverClassName(dbType));
            hikariConfig.setJdbcUrl(dbUrl);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);

            // Configure other settings as needed
            configureDataSource(hikariConfig, env);

            return new HikariDataSource(hikariConfig);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(env.getProperty("main.datasource.driverClassName"));
        hikariConfig.setJdbcUrl(env.getProperty("main.datasource.jdbcUrl"));
        hikariConfig.setUsername(env.getProperty("main.datasource.username"));
        hikariConfig.setPassword(env.getProperty("main.datasource.password"));

        configureDataSource(hikariConfig, env);

        return new HikariDataSource(hikariConfig);
    }

    private String constructJdbcUrl(String dbType, URI dbUri) {
        return switch (dbType) {
            case "postgres" -> "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            case "mysql" -> "jdbc:mysql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    private String determineDriverClassName(String dbType) {
        return switch (dbType) {
            case "postgres" -> "org.postgresql.Driver";
            case "mysql" -> "com.mysql.cj.jdbc.Driver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    private void configureDataSource(HikariConfig hikariConfig, Environment env) {
        hikariConfig.setMinimumIdle(env.getProperty("main.datasource.minimum-idle", Integer.class, 1));
        hikariConfig.setMaximumPoolSize(env.getProperty("main.datasource.maximum-pool-size", Integer.class, 10));
        hikariConfig.setConnectionTestQuery(env.getProperty("main.datasource.connection-test-query", "SELECT 1"));
        hikariConfig.setPoolName(env.getProperty("main.datasource.poolName", "HikariCP"));

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        hikariConfig.setAutoCommit(true);
    }
}
