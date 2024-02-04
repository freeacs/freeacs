package com.github.freeacs.dbi.sql;

import com.github.freeacs.dbi.DynamicStatement;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class DynamicStatementWrapper implements AutoCloseable {
    private final DynamicStatement dynamicStatement;
    private final PreparedStatement preparedStatement;

    public DynamicStatementWrapper(AutoCommitResettingConnectionWrapper connectionWrapper, String sql, Object... args)
            throws SQLException {
        if (connectionWrapper == null) {
            throw new IllegalArgumentException("Connection wrapper cannot be null");
        }
        this.dynamicStatement = new DynamicStatement();
        dynamicStatement.addSqlAndArguments(sql, args);
        preparedStatement = dynamicStatement.makePreparedStatement(connectionWrapper.getConnection());
    }

    @Override
    public void close() throws SQLException {
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLException("Failed to close prepared statement", e);
        }
    }
}