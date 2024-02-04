package com.github.freeacs.common.sql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Getter
public class StatementWithTimeoutWrapper implements AutoCloseable {
    private final Statement statement;

    public StatementWithTimeoutWrapper(Connection connection, int queryTimeout) throws SQLException {
        if (connection == null) throw new IllegalArgumentException("Connection cannot be null");
        this.statement = connection.createStatement();
        this.statement.setQueryTimeout(queryTimeout);
    }

    @Override
    public void close() throws SQLException {
        try {
            statement.close();
        } catch (SQLException e) {
            log.error("Failed to close statement", e);
        }
    }
}