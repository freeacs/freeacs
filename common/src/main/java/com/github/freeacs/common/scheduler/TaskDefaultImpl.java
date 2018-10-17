package com.github.freeacs.common.scheduler;

import org.slf4j.Logger;

public abstract class TaskDefaultImpl implements Task {
  private String taskName;
  private long launchTms;
  private Throwable t;
  private boolean running;

  public TaskDefaultImpl(String taskName) {
    this.taskName = taskName;
  }

  @Override
  public void setThisLaunchTms(long launchTms) {
    this.launchTms = launchTms;
  }

  public long getThisLaunchTms() {
    return launchTms;
  }

  @Override
  public String getTaskName() {
    return taskName;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  @Override
  public Throwable getThrowable() {
    return t;
  }

  @Override
  public void setThrowable(Throwable t) {
    this.t = t;
  }

  /**
   * Implement the task here, no need to think of catching errors, it will be caught in th
   * TaskDefaultImpl-run()-method. Also, the running-flag will be set correctly in
   * TaskDefaultImpl-run()-method.
   *
   * @throws Throwable
   */
  public abstract void runImpl() throws Throwable;

  public abstract Logger getLogger();

  public void run() {
    try {
      running = true;
      runImpl();
    } catch (Throwable t) {
      this.t = t;
      getLogger().error(getTaskName() + " - An error occurred: ", t);
    } finally {
      running = false;
    }
  }
}
