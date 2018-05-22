package com.github.freeacs.shell;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class ACSShellDaemon implements Runnable {
	private ACSShell ACSShell = new ACSShell();
	private final DataSource mainDataSource;
	private final DataSource syslogDataSource;
	private String fusionUser;
	private int index; // used to track which instance of a shell-daemon is running
	private boolean idle = true;

	public ACSShellDaemon(DataSource mainDataSource, DataSource syslogDataSource, String fusionUser) {
		this.mainDataSource = mainDataSource;
		this.syslogDataSource = syslogDataSource;
		this.fusionUser = fusionUser;
	}

	public int getCommandsNotRunYet() {
		return ACSShell.getSession().getProcessor().getDaemonCommandSize();
	}

	public void addToRunList(String command) {
		ACSShell.getSession().getProcessor().addDaemonCommand(command);
	}

	@Override
	public void run() {
		ACSShell.mainImpl(new String[] { "-daemon", "-fusionuser", fusionUser }, mainDataSource, syslogDataSource);
	}

	public List<Throwable> getAndResetThrowables() {
		List<Throwable> throwables = ACSShell.getThrowables();
		if (throwables.size() > 0)
			ACSShell.setThrowables(new ArrayList<Throwable>());
		return throwables;
	}

	public ACSShell getACSShell() {
		return ACSShell;
	}

	public Object getMonitor() {
		return ACSShell.getSession().getProcessor().getMonitor();
	}

	public boolean isInitialized() {
		return ACSShell.isInitialized();
	}

	public boolean isIdle() {
		return idle;
	}

	public void setIdle(boolean idle) {
		this.idle = idle;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
