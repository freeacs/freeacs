package com.github.freeacs.dao;
import io.vavr.collection.List;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface ProfileParameterDao {

    @SqlQuery("select unit_type_param_id, profile_id, value from profile_param")
    @RegisterFieldMapper(ProfileParameter.class)
    List<ProfileParameter> getProfileParameters();

    @SqlUpdate("insert into profile_param(profile_id, unit_type_param_id, value) values (:profileId, :unitTypeParamId, :value)")
    void createProfileParameter(@BindBean ProfileParameter profileParameter);

    @SqlUpdate("delete from profile_param where unit_type_param_id = :unitTypeParamId and profile_id = :profileId")
    Integer deleteProfileParameter(@Bind("unitTypeParamId") Long unitTypeParamId, @Bind("profileId") Long profileId);

}
