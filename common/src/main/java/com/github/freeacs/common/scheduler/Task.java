package com.github.freeacs.common.scheduler;

public interface Task extends Runnable {
  void setThisLaunchTms(long launchTms);

  String getTaskName();

  boolean isRunning();

  Throwable getThrowable();

  void setThrowable(Throwable t);
}
