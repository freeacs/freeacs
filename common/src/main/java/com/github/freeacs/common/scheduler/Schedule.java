package com.github.freeacs.common.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Schedule {
	private long ms;
	private ScheduleType scheduleType;
	// If true, the task will wait minimum "ms" (if type is INTERVAL) or an 
	// hour (if type is HOUR) or a day (if type is DAY) before launching task
	// If false, the task will start as soon as possible - according to the rules
	private boolean delayStart;
	private long previousLaunch;
	private long nextLaunch;
	private Task task;
	private long addedToQueueTms;

	/**
	 * @param ms - for ScheduleType.INTERVAL: the number of milliseconds between each run 
	 *    		 - for other ScheduleTypes: the number of millisecond from start of that period 
	 * @param delayStart - if true, delay start of task one period
	 * @param scheduleType - define the type of period (INTERVAL, HOURLY, DAILY, etc)
	 * @param task - What task to perform at the defined schedule
	 */
	public Schedule(long ms, boolean delayStart, ScheduleType scheduleType, Task task) {
		this.ms = ms;
		this.delayStart = delayStart;
		this.scheduleType = scheduleType;
		this.task = task;
	}

	public long getMs() {
		return ms;
	}

	public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public boolean isDelayStart() {
		return delayStart;
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Schedule))
			return false;
		Schedule s = (Schedule) o;
		if (s.getTask().getTaskName().equals(this.getTask().getTaskName()))
			return true;
		else
			return false;
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd HH:mm:ss.SSS");

	public String toString() {
		String type = null;
		if (scheduleType == ScheduleType.INTERVAL) {
			if (ms < 1000)
				type = "Run every " + ms + " milliseconds";
			else if (ms < 60000)
				type = "Run every " + ms / 1000 + " seconds";
			else if (ms < 60 * 60000)
				type = "Run every " + ms / 60000 + " minutes";
			else
				type = "Run every " + ms / (60 * 60000) + " hours";
		} else if (scheduleType == ScheduleType.MINUTELY)
			type = "Run every minute";
		else if (scheduleType == ScheduleType.HOURLY)
			type = "Run every hour";
		else if (scheduleType == ScheduleType.DAILY)
			type = "Run every day";
		return "Next launch: " + sdf.format(new Date(nextLaunch)) + " for task " + task.getTaskName() + " (" + type + ")";
	}

	public Task getTask() {
		return task;
	}

	public long getPreviousLaunch() {
		return previousLaunch;
	}

	public void setPreviousLaunch(long previousLaunch) {
		this.previousLaunch = previousLaunch;
	}

	public long getNextLaunch() {
		return nextLaunch;
	}

	public void setNextLaunch(long nextLaunch) {
		this.nextLaunch = nextLaunch;
	}

	public long getAddedToQueueTms() {
		return addedToQueueTms;
	}

	public void setAddedToQueueTms(long addedToQueueTms) {
		this.addedToQueueTms = addedToQueueTms;
	}

}