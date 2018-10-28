package com.github.freeacs.common.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.function.Supplier;

import static org.quartz.CronScheduleBuilder.cronSchedule;

public class QuartzDsl {

    private final Scheduler scheduler;

    public QuartzDsl() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }

    public Date scheduleCron(final String jobName,
                             final String jobGroup,
                             final String cronExpression,
                             final Supplier<Void> job) throws SchedulerException {
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("job", job);
        final JobDetail jobDetail = JobBuilder
                .newJob(QuartzJob.class)
                .usingJobData(jobDataMap)
                .withIdentity(jobName + "Detail", jobGroup)
                .build();
        final Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName + "Trigger", jobGroup)
                .startNow()
                .withSchedule(cronSchedule(cronExpression))
                .build();
        return scheduler.scheduleJob(jobDetail, trigger);
    }

    public static void main(String[] args) throws SchedulerException {
        QuartzDsl dsl = new QuartzDsl();
        dsl.scheduleCron("Job", "Test", "* * * * * ? *", () -> {
            System.out.println("Hello you");
            return null;
        });
    }
}
