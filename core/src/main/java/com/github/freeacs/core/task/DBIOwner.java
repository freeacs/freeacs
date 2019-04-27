package com.github.freeacs.core.task;

import com.github.freeacs.common.scheduler.Task;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
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

  private DBI dbi;

  private String taskName;
  private boolean running;

  private Throwable throwable;

  DBIOwner(String taskName, DBI dbi) {
    this.taskName = taskName;
    this.dbi = dbi;
  }

  ACS getLatestACS() {
    return dbi.getAcs();
  }

  Identity getIdentity() {
    return dbi.getSyslog().getIdentity();
  }

  DataSource getDataSource() {
    return dbi.getDataSource();
  }

  protected Syslog getSyslog() {
    return dbi.getSyslog();
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
