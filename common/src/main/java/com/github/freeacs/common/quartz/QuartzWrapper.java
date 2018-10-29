package com.github.freeacs.common.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.util.Date;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzWrapper {

  private final Scheduler scheduler;

  public QuartzWrapper() throws SchedulerException {
    scheduler = StdSchedulerFactory.getDefaultScheduler();
  }

  public void init() throws SchedulerException {
    scheduler.start();
  }

  public void shutdown() throws SchedulerException {
    scheduler.shutdown(true);
  }

  public Date scheduleCron(
      final String jobName,
      final String jobGroup,
      final String cronExpression,
      final QuartzJob.Job job)
      throws SchedulerException {
    final JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("job", job);
    final JobDetail jobDetail =
        JobBuilder.newJob(QuartzJob.class)
            .usingJobData(jobDataMap)
            .withIdentity(jobName + "Detail", jobGroup)
            .build();
    final Trigger trigger =
        TriggerBuilder.newTrigger()
            .withIdentity(jobName + "Trigger", jobGroup)
            .startNow()
            .withSchedule(cronSchedule(cronExpression))
            .build();
    return scheduler.scheduleJob(jobDetail, trigger);
  }

  public void scheduleOnce(final String jobName, final String jobGroup, final QuartzJob.Job job)
      throws SchedulerException {
    final JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("job", job);
    final JobDetail jobDetail =
        JobBuilder.newJob(QuartzJob.class)
            .usingJobData(jobDataMap)
            .withIdentity(jobName + "Detail", jobGroup)
            .build();
    final Trigger trigger =
        TriggerBuilder.newTrigger()
            .withIdentity(jobName + "Trigger", jobGroup)
            .withSchedule(simpleSchedule().withIntervalInSeconds(1).withRepeatCount(1))
            .startNow()
            .build();
    scheduler.scheduleJob(jobDetail, trigger);
  }
}
