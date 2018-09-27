package com.github.freeacs.common.scheduler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowScheduleQueue implements Task {

  private static Logger logger = LoggerFactory.getLogger(ShowScheduleQueue.class);

  private String taskName;
  private boolean running = false;
  private Scheduler scheduler;

  public ShowScheduleQueue(String taskName, Scheduler scheduler) {
    this.taskName = taskName;
    this.scheduler = scheduler;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setThisLaunchTms(long launchTms) {
    return;
  }

  @Override
  public void run() {
    running = true;
    List<Schedule> list = scheduler.getScheduleList().getSchedules();
    for (int i = 0; i < list.size(); i++) logger.debug("[" + i + "] " + list.get(i));
    running = false;
  }

  public boolean isRunning() {
    return running;
  }

  public Throwable getThrowable() {
    return null;
  }

  public void setThrowable(Throwable t) {}
}
