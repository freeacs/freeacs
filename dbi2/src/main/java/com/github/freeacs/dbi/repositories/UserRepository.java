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
            u.secret as u_hashed_secret,
            u.fullname as u_full_name,
            u.accesslist as u_access,
            u.is_admin as u_admin
        FROM
            user_ u
        ORDER BY u.id ASC
    """)
    @RegisterBeanMapper(value = User.class, prefix = "u")
    List<User> listUsers();
}
