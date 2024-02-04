package com.github.freeacs.common.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class AutoCommitResettingConnectionWrapper implements AutoCloseable {
    private final Connection connection;
    private final boolean originalAutoCommit;

    public AutoCommitResettingConnectionWrapper(Connection connection, boolean autoCommit) throws SQLException {
        this.connection = connection;
        this.originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(autoCommit);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } finally {
            connection.close();
        }
    }
}
