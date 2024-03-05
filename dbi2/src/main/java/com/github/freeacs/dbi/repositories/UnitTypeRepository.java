package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.UnitType;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface UnitTypeRepository {

    @SqlQuery("""
        SELECT
            ut.id as ut_id,
            ut.name as ut_name,
            ut.vendor as ut_vendor,
            ut.description as ut_description,
            ut.protocol as ut_protocol
        FROM
            unit_type ut
        ORDER BY ut.id ASC
    """)
    @RegisterBeanMapper(UnitType.class)
    List<UnitType> listUnitTypes();
}
