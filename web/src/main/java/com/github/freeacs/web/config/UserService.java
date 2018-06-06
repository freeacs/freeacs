package com.github.freeacs.web.config;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.security.WebUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;

@Service
public class UserService implements UserDetailsService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    private final DataSource mainDs;

    @Autowired
    public UserService(@Qualifier("main") DataSource mainDs) {
        this.mainDs = mainDs;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users;
        try {
            users = new Users(mainDs);
        } catch (SQLException e) {
            logger.error("Failed to create Users object", e);
            throw new IllegalStateException("Failed to create Users object", e);
        }
        User userObject = users.getUnprotected(username);
        if (userObject == null) {
            throw new UsernameNotFoundException(username);
        }
        return new WebUser(userObject);
    }
}
