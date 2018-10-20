package com.github.freeacs.dbi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.freeacs.common.util.DataSourceHelper;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;

public abstract class BaseDBITest {
  protected ACS acs;
  protected DataSource dataSource;

  @Before
  public void init() {
    acs = mock(ACS.class);
    User user = mock(User.class);
    when(acs.getUser()).thenReturn(user);
    when(user.isUnittypeAdmin(any())).thenReturn(true);
    when(user.isProfileAdmin(any(), any())).thenReturn(true);
    when(user.isAdmin()).thenReturn(true);
    dataSource = DataSourceHelper.inMemoryDataSource();
    when(acs.getDataSource()).thenReturn(dataSource);
    DBI dbi = mock(DBI.class);
    when(acs.getDbi()).thenReturn(dbi);
  }

  @After
  public void tearDown() throws SQLException {
    dataSource.unwrap(HikariDataSource.class).close();
  }
}
