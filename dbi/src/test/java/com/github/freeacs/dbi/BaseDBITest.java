package com.github.freeacs.dbi;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.mariadb.jdbc.MariaDbDataSource;
import org.testcontainers.containers.MySQLContainer;
import com.github.freeacs.common.util.DBScriptUtility;
import java.sql.Connection;

public abstract class BaseDBITest {
  protected ACS acs;
  protected Syslog syslog;

  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:5.7.34");

  static MariaDbDataSource dataSource;

  @BeforeAll
  static void beforeAll() throws Exception {
    mysql.start();
    dataSource = new MariaDbDataSource();
    dataSource.setUrl(String.format("jdbc:mariadb://%s:%d/%s", mysql.getHost(), mysql.getFirstMappedPort(), mysql.getDatabaseName()));
    dataSource.setUser(mysql.getUsername());
    dataSource.setPassword(mysql.getPassword());
    Connection connection = dataSource.getConnection();
    DBScriptUtility.runScript("install.sql", connection);
    connection.close();
  }

  @AfterAll
  static void afterAll() {
    mysql.stop();
  }

  @BeforeEach
  public void init() throws Exception {
    Users users = new Users(dataSource);
    User admin = users.getUnprotected("admin");
    Identity identity = new Identity(SyslogConstants.EVENT_DEFAULT, "test", admin);
    syslog = new Syslog(dataSource, identity);
    acs = new ACS(dataSource, syslog);
  }
}

