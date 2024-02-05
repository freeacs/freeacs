package com.github.freeacs.common.hikari;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    void testGetDataSourceWithDirectJdbcUrl() throws URISyntaxException {
        DatabaseConfig config = DatabaseConfig.builder()
                .jdbcUrl("jdbc:mysql://localhost:3306/mydb?useSSL=false")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .username("user")
                .password("pass")
                .build();

        HikariConfig hikariConfig = config.getHikariConfig();
        assertEquals("com.mysql.cj.jdbc.Driver", hikariConfig.getDriverClassName());
        assertEquals("jdbc:mysql://localhost:3306/mydb?useSSL=false", hikariConfig.getJdbcUrl());
        assertEquals("user", hikariConfig.getUsername());
        assertEquals("pass", hikariConfig.getPassword());
        assertEquals(1, hikariConfig.getMinimumIdle());
        assertEquals(10, hikariConfig.getMaximumPoolSize());
        assertEquals("SELECT 1", hikariConfig.getConnectionTestQuery());
        assertEquals("HikariCP", hikariConfig.getPoolName());
    }

    @Test
    void testGetDataSourceWithUri() throws URISyntaxException {
        DatabaseConfig config = DatabaseConfig.builder()
                .jdbcUrl("mysql://user:pass@localhost:3306/mydb?useSSL=false")
                .build();

        HikariConfig hikariConfig = config.getHikariConfig();
        assertEquals("com.mysql.cj.jdbc.Driver", hikariConfig.getDriverClassName());
        assertEquals("jdbc:mysql://localhost:3306/mydb?useSSL=false", hikariConfig.getJdbcUrl());
        assertEquals("user", hikariConfig.getUsername());
        assertEquals("pass", hikariConfig.getPassword());
        assertEquals(1, hikariConfig.getMinimumIdle());
        assertEquals(10, hikariConfig.getMaximumPoolSize());
        assertEquals("SELECT 1", hikariConfig.getConnectionTestQuery());
        assertEquals("HikariCP", hikariConfig.getPoolName());
    }

    @Test
    void testNoJdbcUrl() {
        DatabaseConfig config = DatabaseConfig.builder().build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, config::getHikariConfig);
        assertEquals("JDBC URL is required", exception.getMessage());
    }
}