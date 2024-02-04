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
        // connection settings
        String databaseUrl,
        String jdbcUrl,
        String driverClassName,
        String username,
        String password,
        // pool settings
        Integer minimumIdle,
        Integer maximumPoolSize,
        String connectionTestQuery,
        String poolName) {
    public DataSource getDataSource() throws URISyntaxException {
        if (databaseUrl != null) {
            HikariConfig hikariConfig = getDatabaseUrlBasedConfig();

            return new HikariDataSource(hikariConfig);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(Optional.ofNullable(username).orElse("acs"));
        hikariConfig.setPassword(Optional.ofNullable(password).orElse("acs"));

        configureDataSource(hikariConfig);

        return new HikariDataSource(hikariConfig);
    }

    private HikariConfig getDatabaseUrlBasedConfig() throws URISyntaxException {
        URI dbUri = new URI(databaseUrl);

        String[] userInfoParts = Optional.ofNullable(dbUri.getUserInfo())
                .map(ui -> ui.split(":"))
                .orElse(new String[0]);
        String username = userInfoParts.length > 0 ? userInfoParts[0] : "acs";
        String password = userInfoParts.length > 1 ? userInfoParts[1] : "acs";

        String dbType = dbUri.getScheme().split(":")[0];
        String dbUrl = constructJdbcUrl(dbType, dbUri);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(determineDriverClassName(dbType));
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // Configure other settings as needed
        configureDataSource(hikariConfig);
        return hikariConfig;
    }

    private String constructJdbcUrl(String dbType, URI dbUri) {
        return switch (dbType) {
            case "postgres" -> "jdbc:postgresql://" + dbUri.getHost() + ':' + getDatabasePort(dbUri, 5432) + dbUri.getPath();
            case "mysql" -> "jdbc:mysql://" + dbUri.getHost() + ':' + getDatabasePort(dbUri, 3306) + dbUri.getPath();
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    private static String getDatabasePort(URI dbUri, int number) {
        return String.valueOf(dbUri.getPort() > -1 ? dbUri.getPort() : number);
    }

    private String determineDriverClassName(String dbType) {
        return switch (dbType) {
            case "postgres" -> "org.postgresql.Driver";
            case "mysql" -> "com.mysql.cj.jdbc.Driver";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    private void configureDataSource(HikariConfig hikariConfig) {
        hikariConfig.setMinimumIdle(Optional.ofNullable(minimumIdle).orElse(1));
        hikariConfig.setMaximumPoolSize(Optional.ofNullable(maximumPoolSize).orElse(10));
        hikariConfig.setConnectionTestQuery(Optional.ofNullable(connectionTestQuery).orElse("SELECT 1"));
        hikariConfig.setPoolName(Optional.ofNullable(poolName).orElse("FreeACS"));

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize","250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit","2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        hikariConfig.setAutoCommit(true);
    }
}
