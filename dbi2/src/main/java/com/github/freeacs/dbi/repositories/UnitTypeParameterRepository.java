package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.UnitType;
import com.github.freeacs.dbi.domain.UnitTypeParameter;
import com.github.freeacs.dbi.domain.UnitTypeParameterValue;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.stringtemplate4.UseStringTemplateEngine;

import java.util.List;

public interface UnitTypeParameterRepository {

    @SqlQuery("""
        SELECT
            utp.unit_type_param_id as id,
            utp.name,
            utp.flags,
            utp.unit_type_id
        FROM unit_type_param utp
        WHERE utp.unit_type_id = :unitTypeId
    """)
    @UseStringTemplateEngine
    @RegisterBeanMapper(value = UnitTypeParameter.class)
    List<UnitTypeParameter> getUnitTypeParameters(Integer unitTypeId);

    @SqlUpdate("""
        INSERT INTO unit_type_param (name, flags, unit_type_id)
        VALUES (:name, :flags, :unitTypeId)
    """)
    @GetGeneratedKeys
    Integer insertUnitTypeParameter(@BindBean UnitTypeParameter unitTypeParameter);
}
