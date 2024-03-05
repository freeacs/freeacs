package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface UserRepository {

    @SqlQuery("""
        SELECT
            u.id as u_id,
            u.username as u_username,
            u.hashed_secret as u_hashed_secret,
            u.full_name as u_full_name,
            u.access as u_access,
            u.admin as u_admin
        FROM
            user u
        ORDER BY u.id ASC
    """)
    @RegisterBeanMapper(User.class)
    List<User> listUsers();
}
