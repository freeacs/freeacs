package com.github.freeacs.core;

import com.github.freeacs.common.scheduler.ExecutorWrapper;
import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.common.util.Sleep;
import com.github.freeacs.core.task.DeleteOldJobs;
import com.github.freeacs.core.task.DeleteOldScripts;
import com.github.freeacs.core.task.DeleteOldSyslog;
import com.github.freeacs.core.task.HeartbeatDetection;
import com.github.freeacs.core.task.JobRuleEnforcer;
import com.github.freeacs.core.task.ReportGenerator;
import com.github.freeacs.core.task.ScriptExecutor;
import com.github.freeacs.core.task.TriggerReleaser;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Identity;
import com.github.freeacs.dbi.Syslog;
import com.github.freeacs.dbi.SyslogConstants;
import com.github.freeacs.dbi.User;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreServlet {

  private static Logger log = LoggerFactory.getLogger(CoreServlet.class);
  private final DataSource mainDataSource;
  private final Properties properties;
  private final ExecutorWrapper executorWrapper;

  public CoreServlet(
      DataSource mainDataSource, Properties properties, ExecutorWrapper executorWrapper) {
    this.mainDataSource = mainDataSource;
    this.properties = properties;
    this.executorWrapper = executorWrapper;
  }

  public void destroy() {
    log.info("Server shutdown...");
    Sleep.terminateApplication();
  }

  public void init() {
    try {
      log.info("Server starts...");
      ACSVersionCheck.versionCheck(mainDataSource);
      DBI dbi = getDbi();
      bootHeavyTasks(dbi);
      bootLightTasks(dbi);
    } catch (Throwable t) {
      log.error("Error occured in init(), some daemons may not have been started", t);
    }
  }

  private DBI getDbi() throws SQLException {
    final Users users = new Users(mainDataSource);
    final User adminUser = users.getUnprotected(Users.USER_ADMIN);
    final Identity id = new Identity(SyslogConstants.FACILITY_CORE, "latest", adminUser);
    final Syslog syslog = new Syslog(mainDataSource, id);
    return new DBI(Integer.MAX_VALUE, mainDataSource, syslog);
  }

  private void bootLightTasks(DBI dbi) {
    // Run at 0530 every night - light task
    final DeleteOldJobs deleteOldJobsTask = new DeleteOldJobs("DeleteOldJobs", dbi, properties);
    executorWrapper.scheduleCron(
        "0 30 5 ? * * *",
        (tms) ->
            () -> {
              deleteOldJobsTask.setThisLaunchTms(tms);
              deleteOldJobsTask.run();
            });

    // Run every second - light task
    final JobRuleEnforcer jobRuleEnforcerTaks =
        new JobRuleEnforcer("JobRuleEnforcer", dbi, properties);
    executorWrapper.scheduleCron(
        "* * * * * ? *",
        (tms) ->
            () -> {
              jobRuleEnforcerTaks.setThisLaunchTms(tms);
              jobRuleEnforcerTaks.run();
            });

    if (ACSVersionCheck.triggerSupported) {
      // Run at 30(sec) every minute - light task
      final TriggerReleaser triggerReleaserTask = new TriggerReleaser("TriggerReleaser", dbi);
      executorWrapper.scheduleCron(
          "30 * * ? * * *",
          (tms) ->
              () -> {
                triggerReleaserTask.setThisLaunchTms(tms);
                triggerReleaserTask.run();
              });
    }
    if (ACSVersionCheck.scriptExecutionSupported) {
      // Run every second - light task
      final ScriptExecutor scriptExecutorTask =
          new ScriptExecutor("ScriptExecutor", dbi, properties);
      executorWrapper.scheduleCron(
          "* * * * * ? *",
          (tms) ->
              () -> {
                scriptExecutorTask.setThisLaunchTms(tms);
                scriptExecutorTask.run();
              });
      // Run at 45 every hour - light task
      final DeleteOldScripts deleteOldScriptsTask =
          new DeleteOldScripts("DeleteOldScripts", dbi, properties);
      executorWrapper.scheduleCron(
          "0 45 * ? * * *",
          (tms) ->
              () -> {
                deleteOldScriptsTask.setThisLaunchTms(tms);
                deleteOldScriptsTask.run();
              });
    }
    if (ACSVersionCheck.heartbeatSupported) {
      // Run every 5 minute - moderate task
      final HeartbeatDetection heartbeatDetectionTask =
          new HeartbeatDetection("HeartbeatDetection", dbi);
      executorWrapper.scheduleCron(
          "0 0/5 * ? * * *",
          (tms) ->
              () -> {
                heartbeatDetectionTask.setThisLaunchTms(tms);
                heartbeatDetectionTask.run();
              });
    }
  }

  private void bootHeavyTasks(DBI dbi) {
    // Run at 00 every hour - heavy task
    final ReportGenerator reportGeneratorHourlyTask =
        new ReportGenerator("ReportGeneratorHourly", ScheduleType.HOURLY, dbi, properties);
    executorWrapper.scheduleCron(
        "0 0 * ? * * *",
        (tms) ->
            () -> {
              reportGeneratorHourlyTask.setThisLaunchTms(tms);
              reportGeneratorHourlyTask.run();
            });

    // Run at 0015 every night - very heavy task
    final ReportGenerator reportGeneratorDailyTask =
        new ReportGenerator("ReportGeneratorDaily", ScheduleType.DAILY, dbi, properties);
    executorWrapper.scheduleCron(
        "0 15 0 ? * * *",
        (tms) ->
            () -> {
              reportGeneratorDailyTask.setThisLaunchTms(tms);
              reportGeneratorDailyTask.run();
            });

    // Run at 0500 every night - very heavy task
    final DeleteOldSyslog deleteOldSyslogTask =
        new DeleteOldSyslog("DeleteOldSyslogEntries", dbi, properties);
    executorWrapper.scheduleCron(
        "0 0 5 ? * * *",
        (tms) ->
            () -> {
              deleteOldSyslogTask.setThisLaunchTms(tms);
              deleteOldSyslogTask.run();
            });
  }

  public String health() {
    return "FREEACSOK";
  }
}
