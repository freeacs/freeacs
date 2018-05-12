package com.owera.xaps.monitor.task;

import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.monitor.Properties;
import com.owera.xaps.monitor.SendMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class MonitorHeartbeatTask extends TaskDefaultImpl {

	private static Logger logger = LoggerFactory.getLogger(MonitorHeartbeatTask.class);

	public MonitorHeartbeatTask(String taskName) {
		super(taskName);
	}

	@Override
	public void runImpl() throws Throwable {
		SendMail.send("Fusion Monitoring: " + Properties.getFusionHostname() + " is up and running", "The Fusion Monitor Server on " + Properties.getFusionHostname() + " is up and running",
				new Date());
		logger.info("Heartbeat sent");
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
