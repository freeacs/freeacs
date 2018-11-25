package com.github.freeacs.dbi;

import ch.vorburger.exec.ManagedProcessException;
import com.github.freeacs.common.util.AbstractEmbeddedDataSourceHelper;
import org.junit.After;
import org.junit.Before;

import java.sql.SQLException;

public abstract class BaseDBITest extends AbstractEmbeddedDataSourceHelper {
  protected ACS acs;
  protected Syslog syslog;

  @Before
  public void init() throws SQLException, ManagedProcessException {
    AbstractEmbeddedDataSourceHelper.setUpBeforeClass();
    Users users = new Users(dataSource);
    User admin = users.getUnprotected("admin");
    Identity identity = new Identity(SyslogConstants.EVENT_DEFAULT, "test", admin);
    syslog = new Syslog(dataSource, identity);
    acs = new ACS(dataSource, syslog);
  }

  @After
  public void tearDown() throws Exception {
    AbstractEmbeddedDataSourceHelper.tearDownAfterClass();
  }
}
