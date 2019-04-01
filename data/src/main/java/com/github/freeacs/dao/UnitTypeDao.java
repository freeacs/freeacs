package com.github.freeacs.dao;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UnitTypeDao {

    @SqlQuery("select unit_type_id as id, unit_type_name as name, vendor_name as vendor, description, protocol from unit_type")
    @RegisterFieldMapper(UnitType.class)
    List<UnitType> getUnitTypes();

    @SqlQuery("select unit_type_id as id, unit_type_name as name, vendor_name as vendor, description, protocol from unit_type where unit_type_id = :id")
    @RegisterFieldMapper(UnitType.class)
    Option<UnitType> getUnitTypeById(@Bind("id") Long id);

    @SqlUpdate("insert into unit_type(unit_type_name, vendor_name, description, protocol) values (:name, :vendor, :description, :protocol)")
    @GetGeneratedKeys
    Long createUnitType(@BindBean UnitType unitType);

    @SqlUpdate("delete from unit_type where unit_type_id = :id")
    Integer deleteUnitType(@Bind("id") Long id);

}
