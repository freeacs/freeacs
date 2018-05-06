package com.owera.xaps.monitor.task;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;
import com.owera.xaps.monitor.Properties;
import com.owera.xaps.monitor.SendMail;

/**
 * Responsible for monitoring status of HTTPMonitorTasks.
 * Rule: If status is null, do nothing
 * Rule: If a task changes status, send mail
 * 
 * @author Morten
 *
 */
public class SendEmailTask extends TaskDefaultImpl {

	private Logger logger = new Logger();
	private Map<String, String> lastErrorMessageMap = new HashMap<String, String>();

	public SendEmailTask(String taskName) {
		super(taskName);
	}

	@Override
	public void runImpl() throws Throwable {
		logger.debug("SendEmailTask is started");
		for (MonitorInfo mi : ModuleMonitorTask.getMonitorInfoSet()) {
			String module = mi.getModule();

			// will only send mails about monitor-module, since monitor-ok-servlet
			// sums up all other modules --> a lot fewer mails
			if (!module.equals("monitor"))
				continue;

			String url = mi.getUrl();
			String em = mi.getErrorMessage();
			String status = mi.getStatus();
			logger.debug("Monitoring: SendEmailTask: Monitor status: " + status + ", em:" + em);
			if (status != null) {
				String lastEm = lastErrorMessageMap.get(module);
				if (lastEm == null) {
					try {
						FileReader fr = new FileReader("fusion-monitor-last-error");
						char[] cbuf = new char[10000];
						fr.read(cbuf);
						lastEm = new String(cbuf).trim();
						fr.close();
					} catch (Throwable t) {
						logger.debug("Did not find fusion-monitor-last-error - probably everything was ok on last round");
					}
				}
				if (lastEm == null && em == null) {
					logger.debug("Monitoring: SendEmailTask:  Both errorMessage and lastErrorMessage are null");
					return;
				}

				if ((lastEm == null && em != null) || (lastEm != null && em == null) || !lastEm.trim().equals(em.trim())) {
					url = url.replace(Properties.getMonitorURLBase(), Properties.getFusionURLBase());
					String msg = "URL tested: <a href=" + url + ">" + url + "</a><p>\n";
					if (status.indexOf("OK") == -1)
						msg += em;
					SendMail.sendFusionAlarm(module, status, msg);
					logger.notice("Monitoring: SendEmailTask: Sent email for url " + url + ", status is " + status);
					if (em != null) {
						if (status.equals("OK"))
							logger.warn("Status is OK, but error-message is not null!: " + em);
						FileWriter fw = new FileWriter("fusion-monitor-last-error", false);
						fw.write(em);
						fw.close();
						logger.info("Wrote fusion-monitor-last-error with latest error message");
					} else if (new File("fusion-monitor-last-error").exists()) {
						boolean success = new File("fusion-monitor-last-error").delete();
						logger.info("Deleted (" + success + ") fusion-monitor-last-error, since everything was ok");
					}
					lastErrorMessageMap.put(module, em);
				} else {
					logger.debug("Monitoring: SendEmailTask: ErrorMessage and lastErrorMessage are equal - no action");

				}
			}
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
