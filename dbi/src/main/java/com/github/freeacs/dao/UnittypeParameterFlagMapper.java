package com.github.freeacs.dao;

import com.github.freeacs.dbi.UnittypeParameterFlag;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UnittypeParameterFlagMapper implements ColumnMapper<UnittypeParameterFlag> {
    @Override
    public UnittypeParameterFlag map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        return new UnittypeParameterFlag(r.getString(columnNumber));
    }
}
