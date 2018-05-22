package com.github.freeacs.shell;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class FreeacsShellDaemon implements Runnable {
	private FreeacsShell freeacsShell = new FreeacsShell();
	private final DataSource mainDataSource;
	private final DataSource syslogDataSource;
	private String fusionUser;
	private int index; // used to track which instance of a shell-daemon is running
	private boolean idle = true;

	public FreeacsShellDaemon(DataSource mainDataSource, DataSource syslogDataSource, String fusionUser) {
		this.mainDataSource = mainDataSource;
		this.syslogDataSource = syslogDataSource;
		this.fusionUser = fusionUser;
	}

	public int getCommandsNotRunYet() {
		return freeacsShell.getSession().getProcessor().getDaemonCommandSize();
	}

	public void addToRunList(String command) {
		freeacsShell.getSession().getProcessor().addDaemonCommand(command);
	}

	@Override
	public void run() {
		freeacsShell.mainImpl(new String[] { "-daemon", "-fusionuser", fusionUser }, mainDataSource, syslogDataSource);
	}

	public List<Throwable> getAndResetThrowables() {
		List<Throwable> throwables = freeacsShell.getThrowables();
		if (throwables.size() > 0)
			freeacsShell.setThrowables(new ArrayList<Throwable>());
		return throwables;
	}

	public FreeacsShell getFreeacsShell() {
		return freeacsShell;
	}

	public Object getMonitor() {
		return freeacsShell.getSession().getProcessor().getMonitor();
	}

	public boolean isInitialized() {
		return freeacsShell.isInitialized();
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
