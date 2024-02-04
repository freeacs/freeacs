package com.github.freeacs.common.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementWithTimeoutWrapper implements AutoCloseable {
    private final Statement statement;

    public StatementWithTimeoutWrapper(Connection connection, int queryTimeout) throws SQLException {
        if (connection == null) throw new IllegalArgumentException("Connection cannot be null");
        this.statement = connection.createStatement();
        this.statement.setQueryTimeout(queryTimeout);
    }

    public Statement getStatement() {
        return statement;
    }

    @Override
    public void close() throws SQLException {
        statement.close();
    }
}