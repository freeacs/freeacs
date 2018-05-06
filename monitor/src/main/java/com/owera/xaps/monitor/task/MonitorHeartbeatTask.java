package com.owera.xaps.monitor.task;

import java.util.Date;

import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.monitor.Properties;
import com.owera.xaps.monitor.SendMail;

public class MonitorHeartbeatTask extends TaskDefaultImpl {

	private Logger logger = new Logger();

	public MonitorHeartbeatTask(String taskName) {
		super(taskName);
	}

	@Override
	public void runImpl() throws Throwable {
		SendMail.send("Fusion Monitoring: " + Properties.getFusionHostname() + " is up and running", "The Fusion Monitor Server on " + Properties.getFusionHostname() + " is up and running",
				new Date());
		logger.notice("Heartbeat sent");
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
