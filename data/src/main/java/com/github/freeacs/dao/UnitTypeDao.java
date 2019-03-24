package com.github.freeacs.dao;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@UseClasspathSqlLocator
public interface UnitTypeDao {

    @SqlQuery
    @RegisterFieldMapper(UnitType.class)
    List<UnitType> getUnitTypes();

    @SqlQuery
    @RegisterFieldMapper(UnitType.class)
    Option<UnitType> getUnitType(@Bind("id") Long id);

    @SqlUpdate
    @GetGeneratedKeys
    Long createUnitType(@BindBean UnitType unitType);

    @SqlUpdate
    Integer deleteUnitType(@Bind("id") Long id);

}
