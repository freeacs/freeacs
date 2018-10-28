package com.github.freeacs.core;

import com.github.freeacs.common.quartz.QuartzWrapper;
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
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.ACSVersionCheck;
import java.sql.SQLException;
import java.util.Date;
import javax.sql.DataSource;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreServlet {

  private static Logger log = LoggerFactory.getLogger(CoreServlet.class);
  private final DataSource mainDataSource;
  private final Properties properties;
  private final QuartzWrapper quartzWrapper;

  public CoreServlet(DataSource mainDataSource, Properties properties) throws SchedulerException {
    this.mainDataSource = mainDataSource;
    this.properties = properties;
    this.quartzWrapper = new QuartzWrapper();
  }

  public void destroy() throws SchedulerException {
    quartzWrapper.shutdown();
    log.info("Server shutdown...");
    Sleep.terminateApplication();
  }

  public void init() {
    try {
      quartzWrapper.init();
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

  private void bootLightTasks(DBI dbi) throws SchedulerException {
    // Run at 0530 every night - light task
    final DeleteOldJobs deleteOldJobsTask = new DeleteOldJobs("DeleteOldJobs", dbi, properties);
    final Date scheduledToRunDOJ =
        quartzWrapper.scheduleCron(
            deleteOldJobsTask.getTaskName(),
            "Core Light",
            "0 30 5 ? * * *",
            deleteOldJobsTask::run);
    log.info("Delete Old Jobs scheduled to run at " + scheduledToRunDOJ.toString());

    // Run every second - light task
    final JobRuleEnforcer jobRuleEnforcerTaks =
        new JobRuleEnforcer("JobRuleEnforcer", dbi, properties);
    final Date scheduledToRunJRE =
        quartzWrapper.scheduleCron(
            jobRuleEnforcerTaks.getTaskName(),
            "Core Light",
            "* * * * * ? *",
            jobRuleEnforcerTaks::run);
    log.info("Job Rule Enforcer scheduled to run at " + scheduledToRunJRE.toString());

    if (ACSVersionCheck.triggerSupported) {
      // Run at 30(sec) every minute - light task
      final TriggerReleaser triggerReleaserTask = new TriggerReleaser("TriggerReleaser", dbi);
      final Date scheduledToRun =
          quartzWrapper.scheduleCron(
              triggerReleaserTask.getTaskName(),
              "Core Light",
              "30 * * ? * * *",
              triggerReleaserTask::run);
      log.info("Trigger releaser scheduled to run at " + scheduledToRun.toString());
    }
    if (ACSVersionCheck.scriptExecutionSupported) {
      // Run every second - light task
      final ScriptExecutor scriptExecutorTask =
          new ScriptExecutor("ScriptExecutor", dbi, properties);
      final Date scheduledToRunSE =
          quartzWrapper.scheduleCron(
              scriptExecutorTask.getTaskName(),
              "Core Light",
              "* * * * * ? *",
              scriptExecutorTask::run);
      log.info("Script Executor scheduled to run at " + scheduledToRunSE.toString());
      // Run at 45 every hour - light task
      final DeleteOldScripts deleteOldScriptsTask =
          new DeleteOldScripts("DeleteOldScripts", dbi, properties);
      final Date scheduledToRunDOS =
          quartzWrapper.scheduleCron(
              deleteOldScriptsTask.getTaskName(),
              "Core Light",
              "0 45 * ? * * *",
              deleteOldScriptsTask::run);
      log.info("Delete Old Scripts scheduled to run at " + scheduledToRunDOS.toString());
    }
    if (ACSVersionCheck.heartbeatSupported) {
      // Run every 5 minute - moderate task
      final HeartbeatDetection heartbeatDetectionTask =
          new HeartbeatDetection("HeartbeatDetection", dbi);
      final Date scheduledToRun =
          quartzWrapper.scheduleCron(
              heartbeatDetectionTask.getTaskName(),
              "Core Light",
              "0 0/5 * ? * * *",
              heartbeatDetectionTask::run);
      log.info("Heartbeat Detection scheduled to run at " + scheduledToRun.toString());
    }
  }

  private void bootHeavyTasks(DBI dbi) throws SchedulerException {
    // Run at 00 every hour - heavy task
    final ReportGenerator reportGeneratorHourlyTask =
        new ReportGenerator("ReportGeneratorHourly", ScheduleType.HOURLY, dbi, properties);
    final Date scheduledToRunRGH =
        quartzWrapper.scheduleCron(
            reportGeneratorHourlyTask.getTaskName(),
            "Core Heavy",
            "0 0 * ? * * *",
            reportGeneratorHourlyTask::run);
    log.info("Report Generator Hourly scheduled to run at " + scheduledToRunRGH.toString());

    // Run at 0015 every night - very heavy task
    final ReportGenerator reportGeneratorDailyTask =
        new ReportGenerator("ReportGeneratorDaily", ScheduleType.DAILY, dbi, properties);
    final Date scheduledToRunRPD =
        quartzWrapper.scheduleCron(
            reportGeneratorDailyTask.getTaskName(),
            "Core Heavy",
            "0 15 0 ? * * *",
            reportGeneratorDailyTask::run);
    log.info("Report Generator Daily scheduled to run at " + scheduledToRunRPD.toString());

    // Run at 0500 every night - very heavy task
    final DeleteOldSyslog deleteOldSyslogTask =
        new DeleteOldSyslog("DeleteOldSyslogEntries", dbi, properties);
    final Date scheduledToRunDOS =
        quartzWrapper.scheduleCron(
            deleteOldSyslogTask.getTaskName(),
            "Core Heavy",
            "0 0 5 ? * * *",
            deleteOldSyslogTask::run);
    log.info("Delete Old Syslog scheduled to run at " + scheduledToRunDOS.toString());
  }

  public String health() {
    return "FREEACSOK";
  }
}
