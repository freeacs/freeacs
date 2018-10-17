package com.github.freeacs.core;

import com.github.freeacs.common.scheduler.Schedule;
import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.common.scheduler.Scheduler;
import com.github.freeacs.common.scheduler.ShowScheduleQueue;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.core.task.DeleteOldJobs;
import com.github.freeacs.core.task.DeleteOldScripts;
import com.github.freeacs.core.task.DeleteOldSyslog;
import com.github.freeacs.core.task.HeartbeatDetection;
import com.github.freeacs.core.task.JobRuleEnforcer;
import com.github.freeacs.core.task.ReportGenerator;
import com.github.freeacs.core.task.ScriptExecutor;
import com.github.freeacs.core.task.TriggerReleaser;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreServlet {
  private static Scheduler scheduler;

  private static Logger log = LoggerFactory.getLogger(CoreServlet.class);
  private final DataSource mainDataSource;
  private final DataSource syslogDataSource;
  private final Properties properties;

  public CoreServlet(DataSource mainDataSource, Properties properties) {
    this.mainDataSource = mainDataSource;
    this.syslogDataSource = mainDataSource;
    this.properties = properties;
  }

  public static void destroy() {
    log.info("Server shutdown...");
    Sleep.terminateApplication();
  }

  public void init() {
    try {
      log.info("Server starts...");
      ACSVersionCheck.versionCheck(mainDataSource);
      scheduler = new Scheduler();
      Thread t = new Thread(scheduler);
      t.setName("Core (Scheduler)");
      t.start();
      bootHeavyTasks();
      bootLightTasks();
    } catch (Throwable t) {
      log.error("Error occured in init(), some daemons may not have been started", t);
    }
  }

  private void bootLightTasks() throws SQLException {
    // Run at 0530 every night - light task
    scheduler.registerTask(
        new Schedule(
            (5 * 60 + 30) * 60000,
            false,
            ScheduleType.DAILY,
            new DeleteOldJobs("DeleteOldJobs", mainDataSource, syslogDataSource, properties)));

    // Run every second - light task
    scheduler.registerTask(
        new Schedule(
            1000,
            false,
            ScheduleType.INTERVAL,
            new JobRuleEnforcer("JobRuleEnforcer", mainDataSource, syslogDataSource, properties)));
    if (ACSVersionCheck.triggerSupported) {
      // Run at 30(sec) every minute - light task
      scheduler.registerTask(
          new Schedule(
              30000,
              false,
              ScheduleType.MINUTELY,
              new TriggerReleaser("TriggerReleaser", mainDataSource, syslogDataSource)));
    }
    if (ACSVersionCheck.scriptExecutionSupported) {
      // Run every 100 ms - very light task
      scheduler.registerTask(
          new Schedule(
              100,
              false,
              ScheduleType.INTERVAL,
              new ScriptExecutor("ScriptExecutor", mainDataSource, syslogDataSource, properties)));
      // Run at 45 every hour - light task
      scheduler.registerTask(
          new Schedule(
              45 * 1000,
              false,
              ScheduleType.MINUTELY,
              new DeleteOldScripts(
                  "DeleteOldScripts", mainDataSource, syslogDataSource, properties)));
    }
    if (ACSVersionCheck.heartbeatSupported) {
      // Run every 5 minute - moderate task
      scheduler.registerTask(
          new Schedule(
              5 * 60000,
              false,
              ScheduleType.INTERVAL,
              new HeartbeatDetection("HeartbeatDetection", mainDataSource, syslogDataSource)));
    }
    // Run at 59 every hour - very light task
    scheduler.registerTask(
        new Schedule(
            60000,
            false,
            ScheduleType.HOURLY,
            new ShowScheduleQueue("ShowScheduleQueue", scheduler)));
  }

  private void bootHeavyTasks() throws SQLException {
    // Run at 00 every hour - heavy task
    scheduler.registerTask(
        new Schedule(
            0,
            false,
            ScheduleType.HOURLY,
            new ReportGenerator(
                "ReportGeneratorHourly",
                ScheduleType.HOURLY,
                mainDataSource,
                syslogDataSource,
                properties)));
    // Run at 0015 every night - very heavy task
    scheduler.registerTask(
        new Schedule(
            15 * 60000,
            false,
            ScheduleType.DAILY,
            new ReportGenerator(
                "ReportGeneratorDaily",
                ScheduleType.DAILY,
                mainDataSource,
                syslogDataSource,
                properties)));
    // Run at 0500 every night - very heavy task
    scheduler.registerTask(
        new Schedule(
            5 * 60 * 60000,
            false,
            ScheduleType.DAILY,
            new DeleteOldSyslog(
                "DeleteOldSyslogEntries", mainDataSource, syslogDataSource, properties)));
  }

  public String health() {
    for (Schedule s : scheduler.getScheduleList().getSchedules()) {
      StringBuilder out = new StringBuilder();
      Throwable t = s.getTask().getThrowable();
      if (t != null) {
        out.append(t).append("\n");
        for (StackTraceElement ste : t.getStackTrace()) {
          out.append(ste).append("\n");
        }
        s.getTask().setThrowable(null);
        return out.toString();
      }
    }
    return "FREEACSOK";
  }
}
