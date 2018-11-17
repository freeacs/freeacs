package com.github.freeacs.web.config;

import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.web.security.WebUser;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
  private static Logger logger = LoggerFactory.getLogger(UserService.class);

  private final Users users;

  public UserService(DataSource mainDs) {
    try {
      users = new Users(mainDs);
    } catch (SQLException e) {
      logger.error("Failed to create Users object", e);
      throw new IllegalStateException("Failed to create Users object", e);
    }
  }

  public WebUser loadUserByUsername(String username) {
    User userObject = users.getUnprotected(username);
    if (userObject == null) {
      throw new IllegalArgumentException(username);
    }
    return new WebUser(userObject);
  }
}
