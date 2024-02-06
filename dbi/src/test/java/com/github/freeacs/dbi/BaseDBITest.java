package com.github.freeacs.dbi;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.hazelcast.jet.core.JetTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.mariadb.jdbc.MariaDbDataSource;

public abstract class BaseDBITest extends JetTestSupport implements AbstractMySqlIntegrationTest {
  protected ACS acs;
  protected Syslog syslog;
  protected MariaDbDataSource dataSource;

  @BeforeEach
  public void init() throws Exception {
    dataSource = AbstractMySqlIntegrationTest.getDataSource();
    Users users = new Users(dataSource);
    User admin = users.getUnprotected("admin");
    Identity identity = new Identity(SyslogConstants.EVENT_DEFAULT, "test", admin);
    syslog = new Syslog(dataSource, identity);
    acs = new ACS(dataSource, syslog);
  }
}

