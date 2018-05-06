package com.owera.xaps.core.task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.log.Logger;
import com.owera.xaps.core.Properties;
import com.owera.xaps.shell.XAPSShell;
import com.owera.xaps.shell.XAPSShellDaemon;

/**
 * Provide a container for all the shell daemons available within TR-069 server.
 * 
 * @author morten
 *
 */
public class ShellDaemonPool {

	private static Logger logger = new Logger();

	//	private static List<XAPSShellDaemon> shellDaemonPool = new ArrayList<XAPSShellDaemon>();
	private static Map<String, List<XAPSShellDaemon>> shellDaemonPoolMap = new HashMap<String, List<XAPSShellDaemon>>();

	private static XAPSShellDaemon createNewShellDaemon(ConnectionProperties xapsCp, int index, String fusionUser) throws Throwable {
		XAPSShellDaemon xapsshellDaemon = new XAPSShellDaemon(xapsCp, fusionUser);
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
		while (true) {
			if (xapsshellDaemon.isInitialized())
				break;
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
		List<XAPSShellDaemon> shellDaemonPool = shellDaemonPoolMap.get(fusionUser);
		if (shellDaemonPool == null) {
			shellDaemonPool = new ArrayList<XAPSShellDaemon>();
			shellDaemonPoolMap.put(fusionUser, shellDaemonPool);
		}
		return shellDaemonPool;
	}

	public static synchronized XAPSShellDaemon getShellDaemon(ConnectionProperties xapsCp, String fusionUser) throws Throwable {
		List<XAPSShellDaemon> shellDaemonPool = getShellDaemonPool(fusionUser);
		int poolsize = Properties.getShellScriptPoolSize();
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
				xapsshellDaemon = createNewShellDaemon(xapsCp, i, fusionUser);
				shellDaemonPool.add(xapsshellDaemon);
				break;
			}
		}
		if (xapsshellDaemon != null)
			xapsshellDaemon.setIdle(false);
		return xapsshellDaemon;
	}
}
