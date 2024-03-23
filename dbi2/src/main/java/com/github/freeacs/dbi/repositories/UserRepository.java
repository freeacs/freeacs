package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

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

    @SqlUpdate("""
        INSERT INTO user_ (username, secret, fullname, accesslist, is_admin)
        VALUES (:username, :hashedSecret, :fullName, :access, :admin)
    """)
    @GetGeneratedKeys
    Integer insertUser(@BindBean User user);

    @SqlUpdate("""
        DELETE FROM user_ WHERE id = :id
    """)
    int deleteUser(Integer id);
}
