package com.github.freeacs.web.config;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.security.WebUser;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  public static WebUser loadUserByUsername(DataSource mainDs, String username) {
    Users users;
    try {
      users = new Users(mainDs);
    } catch (SQLException e) {
      LOGGER.error("Failed to create Users object", e);
      throw new IllegalStateException("Failed to create Users object", e);
    }
    User userObject = users.getUnprotected(username);
    if (userObject == null) {
      return null;
    }
    return new WebUser(userObject);
  }
}
