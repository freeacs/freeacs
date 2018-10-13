package com.github.freeacs.syslogserver;

import com.github.freeacs.common.scheduler.Schedule;
import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.common.scheduler.Scheduler;
import com.github.freeacs.common.util.Sleep;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogServlet {

  public static SyslogServer server = null;

  private static Logger logger = LoggerFactory.getLogger(SyslogServlet.class);
  private final DataSource xapsDataSource;
  private final Properties properties;

  public SyslogServlet(DataSource xapsDataSource, Properties properties) {
    this.xapsDataSource = xapsDataSource;
    this.properties = properties;
  }

  public static void destroy() {
    logger.info("Server shutdown...");
    Sleep.terminateApplication();
  }

  public void init() {
    if (server == null) server = new SyslogServer(xapsDataSource, properties);
    if (!SyslogServer.isStarted()) {
      logger.info("Server startup...");
      Thread serverThread = new Thread(server);
      serverThread.setName("Receive-thread");
      serverThread.start();
    }

    Scheduler scheduler = new Scheduler();

    scheduler.registerTask(
        new Schedule(
            60000, true, ScheduleType.INTERVAL, new SummaryLogger("SummaryLogger", properties)));
    scheduler.registerTask(
        new Schedule(60000, true, ScheduleType.INTERVAL, new StateLogger("StateLogger")));
    scheduler.registerTask(
        new Schedule(
            60000, false, ScheduleType.INTERVAL, new DiskSpaceCheck("DiskSpaceCheck", properties)));

    Thread t = new Thread(scheduler);
    t.setName("Syslog (Scheduler)");
    t.start();
  }

  public String health() {
    StringBuilder status = new StringBuilder();
    if (!FailoverFileReader.isOk()) {
      status = new StringBuilder(FailoverFileReader.getThrowable() + "\n");
      for (StackTraceElement ste : FailoverFileReader.getThrowable().getStackTrace())
        status.append(ste.toString()).append("\n\n");
      status.append("\n");
    }

    if (!Syslog2DB.isOk()) {
      status.append(Syslog2DB.getThrowable()).append("\n");
      for (StackTraceElement ste : Syslog2DB.getThrowable().getStackTrace())
        status.append(ste.toString()).append("\n");
      status.append("\n");
    }
    if (!SyslogServer.isOk()) {
      status.append(SyslogServer.getThrowable()).append("\n");
      for (StackTraceElement ste : SyslogServer.getThrowable().getStackTrace())
        status.append(ste.toString()).append("\n");
      status.append("\n");
    }
    if (status.toString().equals("")) status = new StringBuilder("FREEACSOK");
    return status.toString();
  }
}
