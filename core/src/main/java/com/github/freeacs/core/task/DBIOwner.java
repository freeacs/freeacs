package com.github.freeacs.core.task;

import com.github.freeacs.common.scheduler.Task;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.Users;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;

/**
 * You can extend this object if you need to manipulate the state of the ACS object, then you will
 * not cause concurrent modification problems for other threads extending the same object (as would
 * happen if you extended DBIShare).
 *
 * @author Morten
 */
public abstract class DBIOwner implements Task {
  private final DataSource mainDataSource;
  private final DataSource syslogDataSource;

  private DBI dbi;
  private Syslog syslog;

  private String taskName;
  private boolean running;

  private Throwable throwable;

  public DBIOwner(String taskName, DataSource mainDataSource, DataSource syslogDataSource)
      throws SQLException {
    this.mainDataSource = mainDataSource;
    this.syslogDataSource = syslogDataSource;
    this.taskName = taskName;
    Users users = new Users(mainDataSource);
    Identity id =
        new Identity(
            SyslogConstants.FACILITY_CORE, "latest", users.getUnprotected(Users.USER_ADMIN));
    syslog = new Syslog(syslogDataSource, id);
    dbi = new DBI(Integer.MAX_VALUE, mainDataSource, syslog);
  }

  protected ACS getLatestACS() {
    return dbi.getAcs();
  }

  protected DataSource getSyslogDataSource() {
    return syslogDataSource;
  }

  protected Identity getIdentity() {
    return syslog.getIdentity();
  }

  protected DataSource getMainDataSource() {
    return mainDataSource;
  }

  protected Syslog getSyslog() {
    return syslog;
  }

  public void run() {
    try {
      running = true;
      runImpl();
      running = false;
    } catch (Throwable t) {
      running = false;
      throwable = t;
      getLogger().error("An error occurred", t);
    }
  }

  public boolean isRunning() {
    return running;
  }

  public abstract void runImpl() throws Exception;

  public abstract Logger getLogger();

  @Override
  public void setThisLaunchTms(long launchTms) {}

  @Override
  public String getTaskName() {
    return taskName;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }
}
