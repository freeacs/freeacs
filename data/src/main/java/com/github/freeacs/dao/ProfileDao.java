package com.github.freeacs.dao;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface ProfileDao {

    @SqlQuery("select profile_id as id, profile_name as name, unit_type_id from profile")
    @RegisterFieldMapper(Profile.class)
    List<Profile> getProfiles();

    @SqlQuery("select profile_id as id, profile_name as name, unit_type_id from profile where profile_id = :id")
    @RegisterFieldMapper(Profile.class)
    Option<Profile> getProfile(@Bind("id") Long id);

    @SqlUpdate("insert into profile(profile_name, unit_type_id) values (:name, :unitTypeId)")
    @GetGeneratedKeys
    Long createProfile(@BindBean Profile profile);

    @SqlUpdate("delete from profile where profile_id = :id")
    Integer deleteProfile(@Bind("id") Long id);
}
