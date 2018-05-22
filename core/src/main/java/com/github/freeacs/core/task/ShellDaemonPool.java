package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.shell.ACSShell;
import com.github.freeacs.shell.ACSShellDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide a container for all the shell daemons available within TR-069 server.
 * 
 * @author morten
 *
 */
public class ShellDaemonPool {

	private static Logger logger = LoggerFactory.getLogger(ShellDaemonPool.class);

	private static Map<String, List<ACSShellDaemon>> shellDaemonPoolMap = new HashMap<String, List<ACSShellDaemon>>();

	private static ACSShellDaemon createNewShellDaemon(DataSource mainDataSource, DataSource syslogDataSource, int index, String fusionUser) throws Throwable {
		ACSShellDaemon ACSShellDaemon = new ACSShellDaemon(mainDataSource, syslogDataSource, fusionUser);
		ACSShellDaemon.setIndex(index);
		ACSShell ACSShell = ACSShellDaemon.getACSShell();
		try {
			ACSShell.setPrinter(new PrintWriter(new FileWriter("fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log")));
		} catch (IOException e) {
			logger.error("ScriptExecutor: Cannot log freeacs-shell output til fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log file", e);
		}
		Thread thread = new Thread(ACSShellDaemon);
		thread.setName("Core Shell Daemon " + index);
		thread.setDaemon(true);
		thread.start();
		while (!ACSShellDaemon.isInitialized()) {
			List<Throwable> throwables = ACSShellDaemon.getAndResetThrowables();
			if (throwables != null && throwables.size() > 0) {
				throw throwables.get(0);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ACSShellDaemon;
	}

	private static List<ACSShellDaemon> getShellDaemonPool(String fusionUser) {
		return shellDaemonPoolMap.computeIfAbsent(fusionUser, k -> new ArrayList<>());
	}

	public static synchronized ACSShellDaemon getShellDaemon(DataSource mainDataSource, DataSource syslogDataSource, String fusionUser) throws Throwable {
		List<ACSShellDaemon> shellDaemonPool = getShellDaemonPool(fusionUser);
		int poolsize = Properties.SHELL_SCRIPT_POOL_SIZE;
		ACSShellDaemon ACSShellDaemon = null;
		// Check if any shell daemon is available. If not create a new one within poolsize-limit
		for (int i = 0; i < poolsize; i++) {
			if (shellDaemonPool.size() > i) {
				ACSShellDaemon = shellDaemonPool.get(i);
				if (ACSShellDaemon.isIdle()) {
					break;
				} else {
					ACSShellDaemon = null;
					continue;
				}
			} else {
				ACSShellDaemon = createNewShellDaemon(mainDataSource, syslogDataSource, i, fusionUser);
				shellDaemonPool.add(ACSShellDaemon);
				break;
			}
		}
		if (ACSShellDaemon != null)
			ACSShellDaemon.setIdle(false);
		return ACSShellDaemon;
	}
}
