package de.javawi.jstun.test.demo;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StabilityLogger extends TaskDefaultImpl {

	public StabilityLogger(String taskName) {
		super(taskName);
	}

	private static Logger logger = LoggerFactory.getLogger(StabilityLogger.class);
	private static Logger stability = LoggerFactory.getLogger("Stability");
	private static int summaryHeaderCount = 0;
	
	@Override
	public void runImpl() throws Throwable {
		Counter counter = StunServer.getCounter().cloneAndReset();
		if (summaryHeaderCount == 0) {
			stability.info("Request | ReqNoChg | ReqBindConn | Error | Kick | Idle(#) | ReceiveTime(ms) | ProcessTime(ms) | ActiveDevices ");
			stability.info("------------------------------------------------------------------------------------------------------------- ");
		}
		summaryHeaderCount++;
		if (summaryHeaderCount == 20)
			summaryHeaderCount = 0;

		String message = "";
		message += String.format("%7s | ", counter.getRequest());
		message += String.format("%8s | ", counter.getRequestBindingNoChange());
		message += String.format("%11s | ", counter.getRequestBindingConnection());
		message += String.format("%5s | ", counter.getError());
		message += String.format("%4s | ", counter.getKick());
		message += String.format("%7s | ", counter.getIdle());
		message += String.format("%15s | ", counter.getReceiveTimeNs()/1000000);
		message += String.format("%15s | ", counter.getProcessTimeMs());
		message += String.format("%13s", StunServer.getActiveStunClients().size());
		stability.info(message);

	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
