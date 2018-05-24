package com.github.freeacs.dao;

import com.github.freeacs.dbi.ProvisioningProtocol;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UnittypeProvisioningProtocolMapper implements ColumnMapper<ProvisioningProtocol> {
    @Override
    public ProvisioningProtocol map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        return ProvisioningProtocol.toEnum(r.getString(columnNumber));
    }
}
