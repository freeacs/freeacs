package com.github.freeacs.common.scheduler;

public interface Task extends Runnable {
	public void setThisLaunchTms(long launchTms);

	public String getTaskName();

	public boolean isRunning();

	public Throwable getThrowable();

	public void setThrowable(Throwable t);
}
