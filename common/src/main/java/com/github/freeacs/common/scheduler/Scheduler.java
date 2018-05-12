package com.github.freeacs.common.scheduler;

import com.github.freeacs.common.util.Sleep;

import java.util.Calendar;

/**
 * The idea is to make a class with the sole responsibility of executing certain tasks at
 * specific time of day or at specific interval.  
 * 
 * @author Morten
 *
 */
public class Scheduler implements Runnable {

	private static Calendar calendar = Calendar.getInstance();
	private ScheduleList scheduleList = new ScheduleList();

	private void reset(Calendar c, long now, ScheduleType scheduleType) {
		c.setTimeInMillis(now);
		c.set(Calendar.MILLISECOND, 0);
		if (scheduleType == ScheduleType.DAILY || scheduleType == ScheduleType.HOURLY || scheduleType == ScheduleType.MINUTELY)
			c.set(Calendar.SECOND, 0);
		if (scheduleType == ScheduleType.DAILY || scheduleType == ScheduleType.HOURLY)
			c.set(Calendar.MINUTE, 0);
		if (scheduleType == ScheduleType.DAILY)
			c.set(Calendar.HOUR_OF_DAY, 0);
	}

	private void computeNextTms(Schedule schedule) {
		long now = System.currentTimeMillis(); // Use one timestamp for the entire calculation 
		if (schedule.getScheduleType() == ScheduleType.INTERVAL) {
			if (schedule.getPreviousLaunch() == 0) {
				if (schedule.isDelayStart()) {
					schedule.setNextLaunch(now + schedule.getMs());
				} else {
					schedule.setNextLaunch(now);
				}
			} else {
				schedule.setNextLaunch(schedule.getPreviousLaunch() + schedule.getMs());
				int i = 1;
				while (schedule.getNextLaunch() < now) {
					schedule.setNextLaunch(schedule.getPreviousLaunch() + schedule.getMs() * i);
					i++;
				}
			}
		} else {
			reset(calendar, now, schedule.getScheduleType());
			calendar.add(Calendar.MILLISECOND, (int) schedule.getMs());
			if (schedule.getScheduleType() == ScheduleType.MINUTELY) {
				if (calendar.getTimeInMillis() < now)
					calendar.add(Calendar.MINUTE, 1);
				if (schedule.getPreviousLaunch() == calendar.getTimeInMillis() || (schedule.getPreviousLaunch() == 0 && schedule.isDelayStart()))
					calendar.add(Calendar.MINUTE, 1);
			} else if (schedule.getScheduleType() == ScheduleType.HOURLY) {
				if (calendar.getTimeInMillis() < now)
					calendar.add(Calendar.HOUR_OF_DAY, 1);
				if (schedule.getPreviousLaunch() == calendar.getTimeInMillis() || (schedule.getPreviousLaunch() == 0 && schedule.isDelayStart()))
					calendar.add(Calendar.HOUR_OF_DAY, 1);
			} else if (schedule.getScheduleType() == ScheduleType.DAILY) {
				if (calendar.getTimeInMillis() < now)
					calendar.add(Calendar.DAY_OF_MONTH, 1);
				if (schedule.getPreviousLaunch() == calendar.getTimeInMillis() || (schedule.getPreviousLaunch() == 0 && schedule.isDelayStart()))
					calendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			schedule.setNextLaunch(calendar.getTimeInMillis());
		}
	}

	public void registerTask(Schedule schedule) {
		computeNextTms(schedule);
		scheduleList.add(schedule);
	}
	
	public void unregisterTask(Schedule schedule) {
		scheduleList.remove(schedule);
	}

	public ScheduleList getScheduleList() {
		return scheduleList;
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (Sleep.isTerminated())
					return;

				Schedule schedule = scheduleList.peek();
				if (schedule == null) {
					Thread.sleep(1000);
					continue;
				}
				long timeToLaunch = schedule.getNextLaunch() - System.currentTimeMillis();
				if (timeToLaunch > 1000) {
					Thread.sleep(1000);
					continue;
				} else if (timeToLaunch > 0) {
					Thread.sleep(timeToLaunch);
					continue;
				}
				// This schedule *could* be a new object, compared to the object in the peek() - it could
				// happen if a new task was scheduled in between. However, that doesn't matter - we should
				// process this new schedule first anyhow, since it's overdue if it's first in the list
				// at this point.
				schedule = scheduleList.pop();
				long thisLaunch = schedule.getNextLaunch();
				schedule.setPreviousLaunch(thisLaunch);
				computeNextTms(schedule);
				scheduleList.add(schedule);
				if (schedule.getTask().isRunning())
					continue;
				schedule.getTask().setThisLaunchTms(thisLaunch);
				Thread t = new Thread(schedule.getTask());
				t.setName(schedule.getTask().getTaskName());
				t.start();
			} catch (Throwable t) {

			}
		}
	}
}
