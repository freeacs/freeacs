package com.github.freeacs.shell;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class XAPSShellDaemon implements Runnable {
	private XAPSShell xapsShell = new XAPSShell();
	private DataSource cp;
	private String fusionUser;
	private int index; // used to track which instance of a shell-daemon is running
	private boolean idle = true;

	public XAPSShellDaemon(DataSource cp, String fusionUser) {
		this.cp = cp;
		this.fusionUser = fusionUser;
	}

	public int getCommandsNotRunYet() {
		return xapsShell.getSession().getProcessor().getDaemonCommandSize();
	}

	public List<String> getProcessedCommands() {
		return xapsShell.getSession().getProcessor().getProcessedCommands();
	}

	public String getLastProcessedCommand() {
		return xapsShell.getSession().getProcessor().getLastProcessedCommand();
	}

	public void addToRunList(String command) {
		xapsShell.getSession().getProcessor().addDaemonCommand(command);
	}

	@Override
	public void run() {
		// TODO
		// xapsShell.mainImpl(new String[] { "-daemon", "-url", cp.getUrl(), "-user", cp.getUser(), "-password", cp.getPassword(), "-maxconn", "" + cp.getMaxConn(), "-fusionuser", fusionUser });
	}

	public List<Throwable> getAndResetThrowables() {
		List<Throwable> throwables = xapsShell.getThrowables();
		if (throwables.size() > 0)
			xapsShell.setThrowables(new ArrayList<Throwable>());
		return throwables;
	}

	public XAPSShell getXapsShell() {
		return xapsShell;
	}

	public Object getMonitor() {
		return xapsShell.getSession().getProcessor().getMonitor();
	}

	public boolean isInitialized() {
		return xapsShell.isInitialized();
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
