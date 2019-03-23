package com.github.freeacs.dao;
import io.vavr.collection.List;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@UseClasspathSqlLocator
public interface UnitDao {

    @SqlQuery
    @RegisterFieldMapper(Unit.class)
    List<Unit> getUnits();

    @SqlUpdate
    void createUnit(@BindBean Unit unit);

    @SqlUpdate
    Integer deleteUnit(@Bind("id") String id);

}
