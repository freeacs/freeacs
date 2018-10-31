package com.github.freeacs.common.scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Function;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorWrapper {
  private static final Logger LOG = LoggerFactory.getLogger(ExecutorWrapper.class);

  private final ScheduledExecutorService executorService;

  public ExecutorWrapper(int numThreads) {
    this(numThreads, "FreeACS-Executor", null);
  }

  public ExecutorWrapper(
      int numThreads,
      final String name,
      final Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
    final BasicThreadFactory.Builder factory =
        new BasicThreadFactory.Builder().namingPattern(name + "-%d");
    if (uncaughtExceptionHandler != null) {
      factory.uncaughtExceptionHandler(uncaughtExceptionHandler);
    } else {
      factory.uncaughtExceptionHandler((thread, error) -> {
        LOG.error("Thread " + thread.toString() + " failed to complete properly", error);
      });
    }
    executorService = Executors.newScheduledThreadPool(numThreads, factory.build());
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
