package com.github.freeacs.dao;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UnitDao {

    @SqlQuery("select unit_id, unit_type_id, profile_id from unit")
    @RegisterFieldMapper(Unit.class)
    List<Unit> getUnits();

    @SqlQuery("select unit_id, unit_type_id, profile_id from unit " +
            "where unit_id = :id")
    @RegisterFieldMapper(Unit.class)
    Option<Unit> getUnit(@Bind("id") String id);

    @SqlUpdate("insert into unit(unit_id, unit_type_id, profile_id) " +
            "values (:unitId, :unitTypeId, :profileId)")
    void createUnit(@BindBean Unit unit);

    @SqlUpdate("delete from unit where unit_id = :id\n")
    Integer deleteUnit(@Bind("id") String id);

    @SqlQuery("select unit_id, unit_type_id, profile_id from unit " +
            "where unit_id like concat('%',:term,'%') and profile_id in (<profiles>) " +
            "LIMIT :limit")
    @RegisterFieldMapper(Unit.class)
    List<Unit> searchForUnits(@Bind("term") String term,
                              @BindList("profiles") List<Long> profiles,
                              @Bind("limit") Integer limit);

}
