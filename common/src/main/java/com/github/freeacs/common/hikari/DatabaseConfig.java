package com.github.freeacs.common.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Builder;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Builder
public record DatabaseConfig(
        String databaseUrl,
        String jdbcUrl,
        String driverClassName,
        String username,
        String password,
        Integer minimumIdle,
        Integer maximumPoolSize,
        String connectionTestQuery,
        String poolName,
        Boolean cachePrepStmts,
        Integer prepStmtCacheSize,
        Integer prepStmtCacheSqlLimit,
        Boolean useServerPrepStmts,
        Boolean autoCommit) {
    public DataSource getDataSource() throws URISyntaxException {
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
            configureDataSource(hikariConfig);

            return new HikariDataSource(hikariConfig);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        configureDataSource(hikariConfig);

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

    private void configureDataSource(HikariConfig hikariConfig) {
        hikariConfig.setMinimumIdle(
                Optional.ofNullable(minimumIdle).orElse(1));
        hikariConfig.setMaximumPoolSize(
                Optional.ofNullable(maximumPoolSize).orElse(10));
        hikariConfig.setConnectionTestQuery(
                Optional.ofNullable(connectionTestQuery).orElse("SELECT 1"));
        hikariConfig.setPoolName(
                Optional.ofNullable(poolName).orElse("HikariCP"));

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts",
                Optional.ofNullable(cachePrepStmts).map(Object::toString).orElse( "true"));
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize",
                Optional.ofNullable(prepStmtCacheSize).map(Object::toString).orElse("250"));
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit",
                Optional.ofNullable(prepStmtCacheSqlLimit).map(Object::toString).orElse("2048"));
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts",
                Optional.ofNullable(useServerPrepStmts).map(Object::toString).orElse("true"));

        hikariConfig.setAutoCommit(true);
    }
}
