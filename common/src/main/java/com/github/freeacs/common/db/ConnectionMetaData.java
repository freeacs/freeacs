package com.github.freeacs.common.db;

public class ConnectionMetaData {
	private int accessed;
	private int thrownout;
	private int sqlex;
	private int retries;
	private int denied;
	private long startTms;
	private long usedTime;
	// We make this int-array big enough to accommodate 1000 parallell
	// connections, something
	// we consider highly unlikely.
	private int[] accessedSim = new int[1000];

	public ConnectionMetaData() {
		startTms = System.currentTimeMillis();
	}

	private ConnectionMetaData(long startTms) {
		this.startTms = startTms;
	}

	public synchronized ConnectionMetaData clone() {
		ConnectionMetaData cmd = new ConnectionMetaData(this.startTms);
		cmd.accessed = this.accessed;
		cmd.denied = this.denied;
		cmd.accessedSim = new int[this.accessedSim.length];
		for (int i = 0; i < this.accessedSim.length; i++)
			cmd.accessedSim[i] = this.accessedSim[i];
		cmd.usedTime = this.usedTime;
		return cmd;
	}

	public int getAccessed() {
		return accessed;
	}

	public int getDenied() {
		return denied;
	}

	public long getStartTms() {
		return startTms;
	}

	public synchronized void incDenied() {
		denied++;
	}

	public synchronized void incRetries() {
		retries++;
	}

	public synchronized void incAccessed(int usedSim) {
		accessed++;
		accessedSim[usedSim]++;		
	}

	public synchronized void incSqlEx() {
		sqlex++;
	}
	
	public synchronized void incThrownOut() {
		thrownout++;
	}
	
	public float calculateDeniedPercent() {
		return 100 * (float) denied / ((float) accessed + (float) denied);
	}

	public float calculateUsedPercent() {
		return 100 * (float) usedTime / ((float) (System.currentTimeMillis() - startTms));
	}

	public synchronized void addUsedTime(long used) {
		usedTime += used;
	}

	public long getUsedTime() {
		return usedTime;
	}

	public int[] getAccessedSim() {
		return accessedSim;
	}

	public int getSqlex() {
		return sqlex;
	}

	public int getThrownout() {
		return thrownout;
	}

	public int getRetries() {
		return retries;
	}
}
