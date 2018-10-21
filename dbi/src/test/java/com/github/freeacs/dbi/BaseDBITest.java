package com.github.freeacs.dbi;

import com.github.freeacs.common.util.DataSourceHelper;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;

public abstract class BaseDBITest {
  protected ACS acs;
  protected DataSource dataSource;
  protected Syslog syslog;

  @Before
  public void init() throws SQLException {
    dataSource = DataSourceHelper.inMemoryDataSource();
    Users users = new Users(dataSource);
    User admin = new User("admin", "Admin", "Admin", true, users);
    users.addOrChange(admin, admin); // nice little unit test trick, the user is creating itself
    Identity identity = new Identity(SyslogConstants.EVENT_DEFAULT, "test", admin);
    syslog = new Syslog(dataSource, identity);
    acs = new ACS(dataSource, syslog);
  }

  @After
  public void tearDown() throws SQLException {
    dataSource.unwrap(HikariDataSource.class).close();
  }
}
