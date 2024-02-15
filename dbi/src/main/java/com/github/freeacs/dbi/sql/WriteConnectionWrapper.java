package com.github.freeacs.dbi.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class WriteConnectionWrapper extends AutoCommitResettingConnectionWrapper {
    public WriteConnectionWrapper(Connection connection) throws SQLException {
        super(connection, false, true);
    }
}
