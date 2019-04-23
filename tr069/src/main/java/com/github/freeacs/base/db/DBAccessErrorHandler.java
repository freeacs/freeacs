package com.github.freeacs.base.db;

import com.github.freeacs.base.Log;

import java.sql.SQLException;

abstract class DBAccessErrorHandler {
    static void handleError(String method, Throwable t) throws SQLException {
        Log.error(DBAccess.class, method + " failed", t);
        if (t instanceof SQLException) {
            throw (SQLException) t;
        }
        throw (RuntimeException) t;
    }
}
