package com.github.freeacs.common.scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Function;
import org.quartz.*;

public class ExecutorWrapper {

  private final ScheduledExecutorService executorService;

  public ExecutorWrapper(int numThreads) {
    executorService = Executors.newScheduledThreadPool(numThreads);
  }

  public void shutdown() {
    executorService.shutdown();
  }

  public void scheduleCron(
      final String cronExpression, final Function<Long, Runnable> jobSupplier) {
    scheduleCron(cronExpression, 0, 500, jobSupplier);
  }

  public void scheduleCron(
      final String cronExpression,
      final long initialDelay,
      final long scheduleInterval,
      final Function<Long, Runnable> jobSupplier) {

    final CronExpression cron;
    try {
      cron = new CronExpression(cronExpression);
    } catch (ParseException e) {
      throw new RuntimeException("Failed to parse cron expression: " + cronExpression, e);
    }

    final Runnable scheduleTask =
        new Runnable() {
          private Future<?> lastExecution;
          @Override
          public void run() {
            if (lastExecution != null && !lastExecution.isDone()) {
              return;
            }
            final Date currentTime = new Date();
            final Date nextFireTime = cron.getNextValidTimeAfter(currentTime);
            final Runnable runnable = jobSupplier.apply(nextFireTime.getTime());
            final long delayInMs = nextFireTime.getTime() - currentTime.getTime();
            lastExecution = executorService.schedule(runnable, delayInMs, TimeUnit.MILLISECONDS);
          }
        };

    executorService.scheduleAtFixedRate(
        scheduleTask, initialDelay, scheduleInterval, TimeUnit.MILLISECONDS);
  }
}
