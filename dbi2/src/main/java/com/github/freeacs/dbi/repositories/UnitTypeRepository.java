package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.UnitType;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface UnitTypeRepository {

    @SqlQuery("""
        SELECT
            ut.unit_type_id as ut_id,
            ut.unit_type_name as ut_name,
            ut.vendor_name as ut_vendor,
            ut.description as ut_description,
            ut.protocol as ut_protocol
        FROM
            unit_type ut
        ORDER BY ut.unit_type_id ASC
    """)
    @RegisterBeanMapper(value = UnitType.class, prefix = "ut")
    List<UnitType> listUnitTypes();

    @SqlUpdate("""
        INSERT INTO unit_type (unit_type_name, vendor_name, description, protocol)
        VALUES (:name, :vendor, :description, :protocol)
    """)
    @GetGeneratedKeys
    Integer insertUnitType(@BindBean UnitType unitType);

    @SqlUpdate("""
        UPDATE unit_type
        SET unit_type_name = :name,
            vendor_name = :vendor,
            description = :description,
            protocol = :protocol
        WHERE unit_type_id = :id
    """)
    int updateUnitType(@BindBean UnitType unitType);

    @SqlUpdate("""
        DELETE FROM unit_type WHERE unit_type_id = :id
    """)
    int deleteUnitType(Integer id);
}
