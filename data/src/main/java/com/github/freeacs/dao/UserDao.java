package com.github.freeacs.dao;

import io.vavr.control.Option;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface UserDao {

    @SqlQuery("select id, username, secret, fullname, accesslist from user_ where username = :username")
    @RegisterFieldMapper(User.class)
    Option<User> findUserByName(@Bind("username") String username);
}
