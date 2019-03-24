package com.github.freeacs.dao;
import io.vavr.collection.List;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UnitTypeParameterDao {

    @SqlQuery("select unit_type_param_id as id, unit_type_id, name, flags from unit_type_param")
    @RegisterFieldMapper(UnitTypeParameter.class)
    List<UnitTypeParameter> getUnitTypeParameters();

    @SqlUpdate("insert into unit_type_param(unit_type_id, name, flags) values (:unitTypeId, :name, :flags)")
    @GetGeneratedKeys
    Long createUnitTypeParameter(@BindBean UnitTypeParameter unitTypeParameter);

    @SqlUpdate("delete from unit_type_param where unit_type_param_id = :id")
    Integer deleteUnitTypeParameter(@Bind("id") Long id);

}
