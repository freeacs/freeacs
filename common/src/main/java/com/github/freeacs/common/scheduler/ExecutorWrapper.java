package com.github.freeacs.common.scheduler;

import java.util.function.Function;

public interface ExecutorWrapper {
  void scheduleCron(String cronExpression, Function<Long, Runnable> jobSupplier);

  void scheduleCron(
      String cronExpression,
      long initialDelay,
      long scheduleInterval,
      Function<Long, Runnable> jobSupplier);

  void shutdown();
}
