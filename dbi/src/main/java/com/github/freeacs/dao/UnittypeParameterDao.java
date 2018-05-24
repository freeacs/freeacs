package com.github.freeacs.dao;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.dbi.UnittypeParameterFlag;
import org.jdbi.v3.sqlobject.config.*;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface UnittypeParameterDao {
    @SqlQuery("select unit_type_param_id, unit_type_id, name, flags " +
            "from unit_type_param")
    @RegisterFieldMapper(UnittypeParameterVO.class)
    @RegisterColumnMapper(UnittypeParameterFlagMapper.class)
    List<UnittypeParameterVO> get();

    @SqlQuery("select unit_type_param_id, unit_type_id, name, flags " +
            "from unit_type_param " +
            "where unit_type_param_id = :id")
    @RegisterFieldMapper(UnittypeParameterVO.class)
    @RegisterColumnMapper(UnittypeParameterFlagMapper.class)
    Optional<UnittypeParameterVO> get(@Bind("id") Long id);

    @SqlUpdate("insert into unit_type_param(unit_type_id, name, flags) " +
            "values(:unitTypeId, :name, :flags)")
    @RegisterArgumentFactory(UnittypeParameterFlagArgumentFactory.class)
    @GetGeneratedKeys
    Long add(@BindBean UnittypeParameterVO unittypeParameter);

    @SqlUpdate("update unit_type_param set name = :name, flags = :flags " +
            "where unit_type_param_id = :unitTypeParamId")
    @RegisterArgumentFactory(UnittypeParameterFlagArgumentFactory.class)
    Integer update(@BindBean UnittypeParameterVO unittypeParameter);

    @SqlUpdate("delete from unit_type_param where unit_type_param_id = :id")
    Integer delete(@Bind("id") Long id);

}
