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
public interface ProfileDao {

    @SqlQuery
    @RegisterFieldMapper(Profile.class)
    List<Profile> getProfiles();

    @SqlQuery
    @RegisterFieldMapper(Profile.class)
    Option<Profile> getProfile(@Bind("id") Long id);

    @SqlUpdate
    @GetGeneratedKeys
    Long createProfile(@BindBean Profile profile);

    @SqlUpdate
    Integer deleteProfile(@Bind("id") Long id);
}
