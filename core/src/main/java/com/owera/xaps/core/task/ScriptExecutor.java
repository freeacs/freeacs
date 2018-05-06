package com.owera.xaps.core.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.owera.common.db.NoAvailableConnectionException;
import com.owera.common.log.Logger;
import com.owera.xaps.core.Properties;
import com.owera.xaps.dbi.ScriptExecution;
import com.owera.xaps.dbi.ScriptExecutions;
import com.owera.xaps.dbi.Users;
import com.owera.xaps.shell.Processor;
import com.owera.xaps.shell.Session;
import com.owera.xaps.shell.XAPSShellDaemon;

public class ScriptExecutor extends DBIShare {

	public static class ScriptDaemonRunnable implements Runnable {

		private ScriptExecution se;
		private XAPSShellDaemon xapsshellDaemon;
		private ScriptExecutions executions;
		private Logger daemonLogger = new Logger("ShellDaemon");

		public ScriptDaemonRunnable(ScriptExecutions executions, ScriptExecution scriptExecution, XAPSShellDaemon xapsshellDaemon) {
			this.xapsshellDaemon = xapsshellDaemon;
			this.se = scriptExecution;
			this.executions = executions;
		}

		@Override
		public void run() {
			try {
				Session session = xapsshellDaemon.getXapsShell().getSession();
				Processor proc = session.getProcessor();
				String logPrefix = "ScriptExecutor: " + session.getFusionUser() + "-" + xapsshellDaemon.getIndex() + ":  ";

				String name = se.getScriptFile().getName();
				String command = "call \"" + name + "\" " + se.getArguments();
				daemonLogger.debug(logPrefix + "Will run command : " + command);

				proc.setLogger(daemonLogger);
				proc.setLogPrefix(logPrefix);
				session.getContext().setUnittype(se.getUnittype());
				xapsshellDaemon.addToRunList("var initial_tms \"new Date().toString()\"");
				xapsshellDaemon.addToRunList("echo ${initial_tms}");
				xapsshellDaemon.addToRunList(command);
				xapsshellDaemon.addToRunList("listvars | delvar"); // Cleanup of state
				while (true) {
					synchronized (xapsshellDaemon.getMonitor()) {
						try {
							xapsshellDaemon.getMonitor().wait(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (xapsshellDaemon.getCommandsNotRunYet() == 0) {
						break;
					}
				}
				List<Throwable> throwables = xapsshellDaemon.getAndResetThrowables();
				xapsshellDaemon.setIdle(true);
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

	public ScriptExecutor(String taskName) throws SQLException, NoAvailableConnectionException {
		super(taskName);
	}

	private static Logger logger = new Logger();

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
		ScriptExecutions executions = new ScriptExecutions(getXapsCp());
		List<ScriptExecution> executionList = executions.getNotStartedExecutions(getLatestXAPS(), Properties.getShellScriptPoolSize());

		// Organize all script-executions pr fusion-user - they must be executed in separate shell-deamons
		Map<String, List<ScriptExecution>> userMap = new HashMap<String, List<ScriptExecution>>();
		for (ScriptExecution se : executionList) {
			String fusionUser = Users.USER_ADMIN; // This will only happen if no users are defined, then only admin is available
			if (se.getScriptFile().getOwner() != null)
				fusionUser = se.getScriptFile().getOwner().getUsername();
			List<ScriptExecution> list = userMap.get(fusionUser);
			if (list == null) {
				list = new ArrayList<ScriptExecution>();
				userMap.put(fusionUser, list);
			}
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
					XAPSShellDaemon xapsshellDaemon = ShellDaemonPool.getShellDaemon(getXapsCp(), entry.getKey());
					if (xapsshellDaemon == null) {
						logger.debug("No shell daemon available within pool size limit, will try again in 100 ms");
						continue;
					} else {
						logger.debug("Found shell daemon, will initiate execution");
						se.setStartTms(new Date());
						executions.updateExecution(se);
						ScriptDaemonRunnable ser = new ScriptDaemonRunnable(executions, se, xapsshellDaemon);
						Thread t = new Thread(ser);
						t.start();
					}
				}
			}
		}
	}

}
