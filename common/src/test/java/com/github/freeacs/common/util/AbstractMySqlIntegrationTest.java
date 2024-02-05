package com.github.freeacs.common.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mariadb.jdbc.MariaDbDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;

@Testcontainers
public interface AbstractMySqlIntegrationTest {

    @Container
    MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7.34");

    @BeforeAll
    static void beforeAll() throws Exception {
        mysql.start();
        MariaDbDataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
        DBScriptUtility.runScript("mysql/install.sql", connection);
        DBScriptUtility.runScript("seed.sql", connection);
        connection.close();
    }

    @NotNull
    static MariaDbDataSource getDataSource() throws SQLException {
        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(String.format("jdbc:mariadb://%s:%d/%s", mysql.getHost(), mysql.getFirstMappedPort(), mysql.getDatabaseName()));
        dataSource.setUser(mysql.getUsername());
        dataSource.setPassword(mysql.getPassword());
        return dataSource;
    }

    @AfterAll
    static void afterAll() {
        mysql.stop();
    }
}
