package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.shell.FreeacsShell;
import com.github.freeacs.shell.FreeacsShellDaemon;
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

	private static Map<String, List<FreeacsShellDaemon>> shellDaemonPoolMap = new HashMap<String, List<FreeacsShellDaemon>>();

	private static FreeacsShellDaemon createNewShellDaemon(DataSource mainDataSource, DataSource syslogDataSource, int index, String fusionUser) throws Throwable {
		FreeacsShellDaemon freeacsShellDaemon = new FreeacsShellDaemon(mainDataSource, syslogDataSource, fusionUser);
		freeacsShellDaemon.setIndex(index);
		FreeacsShell freeacsShell = freeacsShellDaemon.getFreeacsShell();
		try {
			freeacsShell.setPrinter(new PrintWriter(new FileWriter("fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log")));
		} catch (IOException e) {
			logger.error("ScriptExecutor: Cannot log freeacs-shell output til fusion-core-shell-daemon-for-" + fusionUser + "-" + index + ".log file", e);
		}
		Thread thread = new Thread(freeacsShellDaemon);
		thread.setName("Core Shell Daemon " + index);
		thread.setDaemon(true);
		thread.start();
		while (!freeacsShellDaemon.isInitialized()) {
			List<Throwable> throwables = freeacsShellDaemon.getAndResetThrowables();
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
		return freeacsShellDaemon;
	}

	private static List<FreeacsShellDaemon> getShellDaemonPool(String fusionUser) {
		return shellDaemonPoolMap.computeIfAbsent(fusionUser, k -> new ArrayList<>());
	}

	public static synchronized FreeacsShellDaemon getShellDaemon(DataSource mainDataSource, DataSource syslogDataSource, String fusionUser) throws Throwable {
		List<FreeacsShellDaemon> shellDaemonPool = getShellDaemonPool(fusionUser);
		int poolsize = Properties.SHELL_SCRIPT_POOL_SIZE;
		FreeacsShellDaemon freeacsShellDaemon = null;
		// Check if any shell daemon is available. If not create a new one within poolsize-limit
		for (int i = 0; i < poolsize; i++) {
			if (shellDaemonPool.size() > i) {
				freeacsShellDaemon = shellDaemonPool.get(i);
				if (freeacsShellDaemon.isIdle()) {
					break;
				} else {
					freeacsShellDaemon = null;
					continue;
				}
			} else {
				freeacsShellDaemon = createNewShellDaemon(mainDataSource, syslogDataSource, i, fusionUser);
				shellDaemonPool.add(freeacsShellDaemon);
				break;
			}
		}
		if (freeacsShellDaemon != null)
			freeacsShellDaemon.setIdle(false);
		return freeacsShellDaemon;
	}
}
