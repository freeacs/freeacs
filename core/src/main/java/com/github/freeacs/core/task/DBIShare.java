package com.github.freeacs.core.task;

import com.github.freeacs.common.scheduler.Task;
import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Syslog;
import javax.sql.DataSource;
import org.slf4j.Logger;

/**
 * You can extend DBIShare if, and only if, you do not manipulate the contents of the ACS object.
 * Then you can share the same DBI object - and reduce the load on the system.
 *
 * @author Morten
 */
public abstract class DBIShare implements Task {
  private DBI dbi;

  private String taskName;
  private long launchTms;
  private boolean running;

  private Throwable throwable;

  public DBIShare(String taskName, DBI dbi) {
    this.taskName = taskName;
    this.dbi = dbi;
  }

  protected ACS getLatestACS() {
    return dbi.getAcs();
  }

  protected DataSource getDataSource() {
    return dbi.getDataSource();
  }

  protected long getLaunchTms() {
    return launchTms;
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
  public void setThisLaunchTms(long launchTms) {
    this.launchTms = launchTms;
  }

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
