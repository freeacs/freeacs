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
public interface ProfileDao {

    @SqlQuery
    @RegisterFieldMapper(Profile.class)
    List<Profile> getProfiles();

    @SqlUpdate
    @GetGeneratedKeys
    Long createProfile(@BindBean Profile profile);

    @SqlUpdate
    Integer deleteProfile(@Bind("id") Long id);

}
