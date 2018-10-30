package com.github.freeacs.syslogserver;

import com.github.freeacs.common.quartz.QuartzWrapper;
import com.github.freeacs.common.util.Sleep;
import javax.sql.DataSource;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogServlet {
  public static SyslogServer server;

  private static Logger logger = LoggerFactory.getLogger(SyslogServlet.class);
  private final DataSource xapsDataSource;
  private final Properties properties;
  private final QuartzWrapper quartzWrapper;

  public SyslogServlet(
      DataSource xapsDataSource, Properties properties, QuartzWrapper quartzWrapper) {
    this.xapsDataSource = xapsDataSource;
    this.properties = properties;
    this.quartzWrapper = quartzWrapper;
  }

  public static void destroy() {
    logger.info("Server shutdown...");
    Sleep.terminateApplication();
  }

  public void init() throws SchedulerException {
    if (server == null) {
      server = new SyslogServer(xapsDataSource, properties);
    }
    if (!SyslogServer.isStarted()) {
      logger.info("Server startup...");
      Thread serverThread = new Thread(server);
      serverThread.setName("Receive-thread");
      serverThread.start();
    }

    SummaryLogger summaryLoggerTask = new SummaryLogger("SummaryLogger", properties);
    quartzWrapper.scheduleCron(
        summaryLoggerTask.getTaskName(),
        "Syslog",
        "0 * * ? * * *",
        SummaryLoggerJob.class,
        (tms) -> {
          summaryLoggerTask.setThisLaunchTms(tms);
          summaryLoggerTask.run();
        });
    StateLogger stateLogger = new StateLogger("StateLogger");
    quartzWrapper.scheduleCron(
        stateLogger.getTaskName(),
        "Syslog",
        "15 * * ? * * *",
        StateLoggerJob.class,
        (tms) -> {
          stateLogger.setThisLaunchTms(tms);
          stateLogger.run();
        });
    DiskSpaceCheck diskSpaceCheck = new DiskSpaceCheck("DiskSpaceCheck", properties);
    quartzWrapper.scheduleCron(
        diskSpaceCheck.getTaskName(),
        "Syslog",
        "30 * * ? * * *",
        DiskSpaceCheckJob.class,
        (tms) -> {
          diskSpaceCheck.setThisLaunchTms(tms);
          diskSpaceCheck.run();
        });
  }

  public String health() {
    StringBuilder status = new StringBuilder();
    if (!FailoverFileReader.isOk()) {
      status = new StringBuilder().append(FailoverFileReader.getThrowable()).append("\n");
      for (StackTraceElement ste : FailoverFileReader.getThrowable().getStackTrace()) {
        status.append(ste).append("\n\n");
      }
      status.append("\n");
    }

    if (!Syslog2DB.isOk()) {
      status.append(Syslog2DB.getThrowable()).append("\n");
      for (StackTraceElement ste : Syslog2DB.getThrowable().getStackTrace()) {
        status.append(ste).append("\n");
      }
      status.append("\n");
    }
    if (!SyslogServer.isOk()) {
      status.append(SyslogServer.getThrowable()).append("\n");
      for (StackTraceElement ste : SyslogServer.getThrowable().getStackTrace()) {
        status.append(ste).append("\n");
      }
      status.append("\n");
    }
    if ("".equals(status.toString())) {
      status = new StringBuilder("FREEACSOK");
    }
    return status.toString();
  }
}
