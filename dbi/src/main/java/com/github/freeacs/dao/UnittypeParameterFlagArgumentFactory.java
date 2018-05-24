package com.github.freeacs.dao;

import com.github.freeacs.dbi.UnittypeParameterFlag;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class UnittypeParameterFlagArgumentFactory extends AbstractArgumentFactory<UnittypeParameterFlag> {

    public UnittypeParameterFlagArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(UnittypeParameterFlag value, ConfigRegistry config) {
        return (position, statement, ctx) -> statement.setString(position, value.getFlag());
    }
}
