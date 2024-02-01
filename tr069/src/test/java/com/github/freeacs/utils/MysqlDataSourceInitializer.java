package com.github.freeacs.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;

public interface MysqlDataSourceInitializer {

    public static void initialize(MySQLContainer<?> mysql, @NotNull ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.test.database.replace=none", // Tells Spring Boot not to start in-memory db for tests.
                "main.datasource.jdbcUrl=" + mysql.getJdbcUrl(),
                "main.datasource.username=" + mysql.getUsername(),
                "main.datasource.password=" + mysql.getPassword()
        );
    }
}