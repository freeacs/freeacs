package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.Profile;
import com.github.freeacs.dbi.domain.Unit;
import com.github.freeacs.dbi.domain.UnitType;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.stream.Stream;

public interface UnitRepository {

    @SqlQuery("""
        SELECT
            u.id as u_id,
            p.id as p_id,
            p.name as p_name,
            ut.id as ut_id,
            ut.name as ut_name,
            ut.vendor as ut_vendor,
            ut.description as ut_description,
            ut.protocol as ut_protocol
        FROM
            unit u
        INNER JOIN profile p ON u.profile_id = p.id
        INNER JOIN unit_type ut ON p.unit_type_id = ut.id
        ORDER BY u.id ASC
    """)
    @RegisterBeanMapper(value = Unit.class, prefix = "u")
    @RegisterBeanMapper(value = Profile.class, prefix = "p")
    @RegisterBeanMapper(value = UnitType.class, prefix = "ut")
    Stream<Unit> listUnits();
}
