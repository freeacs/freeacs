package com.owera.common.scheduler;

import java.util.List;

import com.owera.common.log.Logger;

public class ShowScheduleQueue implements Task {

	private Logger logger = new Logger();

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
		for (int i = 0; i < list.size(); i++)
			logger.debug("[" + i + "] " + list.get(i));
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public Throwable getThrowable() {
		return null;
	}

	public void setThrowable(Throwable t) {

	}
}