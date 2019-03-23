package com.github.freeacs.dao;
import io.vavr.collection.List;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@UseClasspathSqlLocator
public interface ProfileParameterDao {

    @SqlQuery
    @RegisterFieldMapper(ProfileParameter.class)
    List<ProfileParameter> getProfileParameters();

    @SqlUpdate
    void createProfileParameter(@BindBean ProfileParameter profileParameter);

    @SqlUpdate
    Integer deleteProfileParameter(@Bind("unitTypeParamId") Long unitTypeParamId, @Bind("profileId") Long profileId);

}
