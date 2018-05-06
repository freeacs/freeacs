package com.owera.xaps.shell;

import java.util.ArrayList;
import java.util.List;

import com.owera.common.db.ConnectionProperties;
import com.owera.common.db.ConnectionProvider;
import com.owera.common.log.Log;

public class XAPSShellDaemon implements Runnable {
	private XAPSShell xapsShell = new XAPSShell();
	private ConnectionProperties cp;
	private String fusionUser;
	private int index; // used to track which instance of a shell-daemon is running
	private boolean idle = true;

	public XAPSShellDaemon(ConnectionProperties cp, String fusionUser) {
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
		xapsShell.mainImpl(new String[] { "-daemon", "-url", cp.getUrl(), "-user", cp.getUser(), "-password", cp.getPassword(), "-maxconn", "" + cp.getMaxConn(), "-fusionuser", fusionUser });
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

	/*
	 * Purely for test
	 */
	public static void main(String[] args) {
		Log.initialize("xaps-shell-logs.properties");
		XAPSShellDaemon daemon = new XAPSShellDaemon(ConnectionProvider.getConnectionProperties("xaps-shell.properties", "db.2"), "teamf1");
		Thread t = new Thread(daemon);
		t.start();
		while (!daemon.isInitialized())
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		daemon.addToRunList("listunittypes > unittypes.txt");
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
