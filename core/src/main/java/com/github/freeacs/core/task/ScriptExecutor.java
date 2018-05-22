package com.github.freeacs.core.task;

import com.github.freeacs.core.Properties;
import com.github.freeacs.dbi.ScriptExecution;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.Users;
import com.github.freeacs.shell.Processor;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.FreeacsShellDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class ScriptExecutor extends DBIShare {

	public static class ScriptDaemonRunnable implements Runnable {

		private ScriptExecution se;
		private FreeacsShellDaemon freeacsShellDaemon;
		private ScriptExecutions executions;
		private static Logger daemonLogger = LoggerFactory.getLogger("ShellDaemon");

		public ScriptDaemonRunnable(ScriptExecutions executions, ScriptExecution scriptExecution, FreeacsShellDaemon freeacsShellDaemon) {
			this.freeacsShellDaemon = freeacsShellDaemon;
			this.se = scriptExecution;
			this.executions = executions;
		}

		@Override
		public void run() {
			try {
				Session session = freeacsShellDaemon.getFreeacsShell().getSession();
				Processor proc = session.getProcessor();
				String logPrefix = "ScriptExecutor: " + session.getFusionUser() + "-" + freeacsShellDaemon.getIndex() + ":  ";

				String name = se.getScriptFile().getName();
				String command = "call \"" + name + "\" " + se.getArguments();
				daemonLogger.debug(logPrefix + "Will run command : " + command);

				proc.setLogger(daemonLogger);
				proc.setLogPrefix(logPrefix);
				session.getContext().setUnittype(se.getUnittype());
				freeacsShellDaemon.addToRunList("var initial_tms \"new Date().toString()\"");
				freeacsShellDaemon.addToRunList("echo ${initial_tms}");
				freeacsShellDaemon.addToRunList(command);
				freeacsShellDaemon.addToRunList("listvars | delvar"); // Cleanup of state
				while (true) {
					synchronized (freeacsShellDaemon.getMonitor()) {
						try {
							freeacsShellDaemon.getMonitor().wait(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (freeacsShellDaemon.getCommandsNotRunYet() == 0) {
						break;
					}
				}
				List<Throwable> throwables = freeacsShellDaemon.getAndResetThrowables();
				freeacsShellDaemon.setIdle(true);
				boolean exitStatus = false;
				String errorMsg = null;
				if (throwables.size() > 0) {
					exitStatus = true;
					errorMsg = "";
					for (Throwable t : throwables) {
						errorMsg += t.getMessage() + "\n";
						daemonLogger.error("ScriptExecutor: Error  in Script execution : ", t);
					}
					if (errorMsg.length() > 1024) {
						errorMsg = errorMsg.substring(0, 1020) + "...";
					}
				}
				se.setEndTms(new Date());
				if (errorMsg != null)
					se.setErrorMessage(errorMsg.trim());
				if (exitStatus)
					daemonLogger.debug("ScriptExecutor: Exit-status ERROR running command : " + command + ", errorMsg: " + errorMsg);
				else
					daemonLogger.debug("ScriptExecutor: Exit-status SUCCESS running command : " + command);
				se.setExitStatus(exitStatus);
				executions.updateExecution(se);
			} catch (Throwable t) {
				daemonLogger.error("ScriptExecutor: Error occured during ScriptExecutionRunnable.run()", t);
			}
		}
	}

	public ScriptExecutor(String taskName, DataSource mainDataSource, DataSource syslogDataSource) throws SQLException {
		super(taskName, mainDataSource, syslogDataSource);
	}

	private static Logger logger = LoggerFactory.getLogger(ScriptExecutor.class);

	@Override
	public void runImpl() throws Exception {
		try {
			processScripts();
		} catch (Throwable t) {
			if (t instanceof Exception)
				throw (Exception) t;
			else {
				throw new Exception(t.getMessage(), t);
			}
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	private void processScripts() throws Throwable {
		ScriptExecutions executions = new ScriptExecutions(getMainDataSource());
		List<ScriptExecution> executionList = executions.getNotStartedExecutions(getLatestFreeacs(), Properties.SHELL_SCRIPT_POOL_SIZE);

		// Organize all script-executions pr fusion-user - they must be executed in separate shell-deamons
		Map<String, List<ScriptExecution>> userMap = new HashMap<String, List<ScriptExecution>>();
		for (ScriptExecution se : executionList) {
			String fusionUser = Users.USER_ADMIN; // This will only happen if no users are defined, then only admin is available
			if (se.getScriptFile().getOwner() != null)
				fusionUser = se.getScriptFile().getOwner().getUsername();
			List<ScriptExecution> list = userMap.computeIfAbsent(fusionUser, k -> new ArrayList<>());
			list.add(se);
		}

		for (Entry<String, List<ScriptExecution>> entry : userMap.entrySet()) {
			List<ScriptExecution> userExecutionList = entry.getValue();
			for (ScriptExecution se : userExecutionList) {
				if (se.getScriptFile() == null) { // The file OR unittype has been deleted
					se.setStartTms(new Date());
					se.setEndTms(se.getStartTms());
					if (se.getUnittype() == null)
						se.setErrorMessage("The unittype from which this script was initiated is deleted, therefore the script is also deleted, aborting script execution");
					else
						se.setErrorMessage("The script is deleted, aborting script execution");
					executions.updateExecution(se);
				} else {
					FreeacsShellDaemon shellDaemon = ShellDaemonPool.getShellDaemon(getMainDataSource(), getSyslogDataSource(), entry.getKey());
					if (shellDaemon == null) {
						logger.debug("No shell daemon available within pool size limit, will try again in 100 ms");
					} else {
						logger.debug("Found shell daemon, will initiate execution");
						se.setStartTms(new Date());
						executions.updateExecution(se);
						ScriptDaemonRunnable ser = new ScriptDaemonRunnable(executions, se, shellDaemon);
						Thread t = new Thread(ser);
						t.start();
					}
				}
			}
		}
	}

}
