package com.github.freeacs.dbi.sql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Getter
public class AutoCommitResettingConnectionWrapper implements AutoCloseable {
    private final Connection connection;
    private final boolean originalAutoCommit;

    public AutoCommitResettingConnectionWrapper(Connection connection, boolean autoCommit)
            throws SQLException, IllegalArgumentException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
        this.originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(autoCommit);
    }

    @Override
    public void close() throws SQLException {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            log.error("Failed to reset auto commit", e);
        } finally {
            connection.close();
        }
    }
}
