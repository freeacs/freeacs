package com.github.freeacs.monitor.task;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import com.github.freeacs.monitor.Properties;
import com.github.freeacs.monitor.SendMail;
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
