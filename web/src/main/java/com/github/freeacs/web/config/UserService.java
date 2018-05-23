package com.github.freeacs.web.config;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.app.security.WebUser;
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
    private final DataSource mainDs;

    @Autowired
    public UserService(@Qualifier("main") DataSource mainDs) {
        this.mainDs = mainDs;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = null;
        try {
            users = new Users(mainDs);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create Users object", e);
        }
        User userObject = users.getUnprotected(username);
        if (userObject == null) {
            throw new UsernameNotFoundException(username);
        }
        return new WebUser(userObject);
    }
}
