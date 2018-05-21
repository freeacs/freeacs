package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.shell.XAPSShell;
import com.github.freeacs.shell.XAPSShellDaemon;
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

	private static Map<String, List<XAPSShellDaemon>> shellDaemonPoolMap = new HashMap<String, List<XAPSShellDaemon>>();

	private static XAPSShellDaemon createNewShellDaemon(DataSource xapsCp, DataSource syslogCp, int index, String fusionUser) throws Throwable {
		XAPSShellDaemon xapsshellDaemon = new XAPSShellDaemon(xapsCp, syslogCp, fusionUser);
		xapsshellDaemon.setIndex(index);
		XAPSShell xapsShell = xapsshellDaemon.getXapsShell();
		try {
			xapsShell.setPrinter(new PrintWriter(new FileWriter("fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log")));
		} catch (IOException e) {
			logger.error("ScriptExecutor: Cannot log xaps-shell output til fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log file", e);
		}
		Thread xapsShellThread = new Thread(xapsshellDaemon);
		xapsShellThread.setName("Core Shell Daemon " + index);
		xapsShellThread.setDaemon(true);
		xapsShellThread.start();
		while (!xapsshellDaemon.isInitialized()) {
			List<Throwable> throwables = xapsshellDaemon.getAndResetThrowables();
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
		return xapsshellDaemon;
	}

	private static List<XAPSShellDaemon> getShellDaemonPool(String fusionUser) {
		return shellDaemonPoolMap.computeIfAbsent(fusionUser, k -> new ArrayList<>());
	}

	public static synchronized XAPSShellDaemon getShellDaemon(DataSource xapsCp, DataSource syslogCp, String fusionUser) throws Throwable {
		List<XAPSShellDaemon> shellDaemonPool = getShellDaemonPool(fusionUser);
		int poolsize = Properties.SHELL_SCRIPT_POOL_SIZE;
		XAPSShellDaemon xapsshellDaemon = null;
		// Check if any shell daemon is available. If not create a new one within poolsize-limit
		for (int i = 0; i < poolsize; i++) {
			if (shellDaemonPool.size() > i) {
				xapsshellDaemon = shellDaemonPool.get(i);
				if (xapsshellDaemon.isIdle()) {
					break;
				} else {
					xapsshellDaemon = null;
					continue;
				}
			} else {
				xapsshellDaemon = createNewShellDaemon(xapsCp, syslogCp, i, fusionUser);
				shellDaemonPool.add(xapsshellDaemon);
				break;
			}
		}
		if (xapsshellDaemon != null)
			xapsshellDaemon.setIdle(false);
		return xapsshellDaemon;
	}
}
