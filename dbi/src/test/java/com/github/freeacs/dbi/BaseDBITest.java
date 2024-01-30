package com.github.freeacs.dbi;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.mariadb.jdbc.MariaDbDataSource;
import org.testcontainers.containers.MySQLContainer;

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
    String path = new java.io.File(ClassLoader.getSystemClassLoader().getResource("install.sql").getFile()).toPath().toString();
    DBScriptUtility.runScript(path, connection);
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

