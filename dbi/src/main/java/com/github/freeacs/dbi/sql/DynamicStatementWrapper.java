package com.github.freeacs.dbi.sql;

import com.github.freeacs.dbi.DynamicStatement;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class DynamicStatementWrapper implements AutoCloseable {
    private final DynamicStatement dynamicStatement;
    private final PreparedStatement preparedStatement;

    public DynamicStatementWrapper(AutoCommitResettingConnectionWrapper connectionWrapper, String sql, Object... args) throws SQLException {
        this.dynamicStatement = new DynamicStatement();
        dynamicStatement.addSqlAndArguments(sql, args);
        preparedStatement = dynamicStatement.makePreparedStatement(connectionWrapper.getConnection());
    }

    @Override
    public void close() throws SQLException {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }
}