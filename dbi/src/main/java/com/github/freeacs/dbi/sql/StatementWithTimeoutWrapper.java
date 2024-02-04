package com.github.freeacs.dbi.sql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Getter
public class StatementWithTimeoutWrapper implements AutoCloseable {
    private final Statement statement;

    public StatementWithTimeoutWrapper(AutoCommitResettingConnectionWrapper connectionWrapper, int queryTimeout)
            throws SQLException, IllegalArgumentException {
        if (connectionWrapper == null) {
            throw new IllegalArgumentException("Connection wrapper cannot be null");
        }
        this.statement = connectionWrapper.getConnection().createStatement();
        this.statement.setQueryTimeout(queryTimeout);
    }

    @Override
    public void close() throws SQLException {
        try {
            statement.close();
        } catch (SQLException e) {
            throw new SQLException("Failed to close statement", e);
        }
    }
}