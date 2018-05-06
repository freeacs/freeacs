package com.owera.xaps.tr069.background;

import com.owera.common.scheduler.Schedule;
import com.owera.common.scheduler.ScheduleType;
import com.owera.common.scheduler.Scheduler;
import com.owera.xaps.dbi.DBI;

public class BackgroundProcesses {

	private static Scheduler scheduler = new Scheduler();

	public static void initiate(DBI dbi) {
		Thread t = new Thread(scheduler);
		t.setName("TR069 (BackgroundProcesses)");
		t.start();
		scheduler.registerTask(new Schedule(10000, false, ScheduleType.INTERVAL, new StabilityTask("StabilityLogger")));
		scheduler.registerTask(new Schedule(5000, false, ScheduleType.INTERVAL, new MessageListenerTask("MessageListener", dbi)));
		scheduler.registerTask(new Schedule(1000, false, ScheduleType.INTERVAL, new ScheduledKickTask("ScheduledKick", dbi)));
		scheduler.registerTask(new Schedule(5 * 60000, false, ScheduleType.INTERVAL, new ActiveDeviceDetectionTask("ActiveDeviceDetection TR069", dbi)));
	}

	public static Scheduler getScheduler() {
		return scheduler;
	}
}
