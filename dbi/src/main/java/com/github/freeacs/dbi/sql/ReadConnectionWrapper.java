package com.github.freeacs.dbi.sql;

public class ReadConnectionWrapper extends AutoCommitResettingConnectionWrapper {
    public ReadConnectionWrapper(java.sql.Connection connection) throws java.sql.SQLException {
        super(connection, false, false);
    }
}
