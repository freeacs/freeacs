package com.github.freeacs.dao;
import io.vavr.collection.List;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@UseClasspathSqlLocator
public interface UnitTypeParameterDao {

    @SqlQuery
    @RegisterFieldMapper(UnitTypeParameter.class)
    List<UnitTypeParameter> getUnitTypeParameters();

    @SqlUpdate
    @GetGeneratedKeys
    Long createUnitTypeParameter(@BindBean UnitTypeParameter unitTypeParameter);

    @SqlUpdate
    Integer deleteUnitTypeParameter(@Bind("id") Long id);

}
