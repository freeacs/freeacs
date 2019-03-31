package com.github.freeacs.dao;

import io.vavr.collection.List;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface PermissionDao {

    @SqlQuery("select id, user_id, unit_type_id, profile_id from permission_ where user_id = :userId")
    @RegisterFieldMapper(Permission.class)
    List<Permission> getByUserId(@Bind("userId") Long userId);

}
