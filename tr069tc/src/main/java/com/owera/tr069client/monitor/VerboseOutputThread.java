package com.owera.tr069client.monitor;

import java.util.ArrayList;
import java.util.List;

public class VerboseOutputThread implements Runnable {

	public final static int SLEEP_BETWEEN_LOGGINGS = 10000;

	private List<Status> statusList = new ArrayList<Status>();

	public void run() {
		
		StatusReport statusReport = new StatusReport();
		long startLoop = System.currentTimeMillis();
		while (true) {
			try {
				long diff = System.currentTimeMillis() - startLoop;
				Thread.sleep(SLEEP_BETWEEN_LOGGINGS - diff);
				startLoop = System.currentTimeMillis();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (this) {
				statusList = statusReport.update(statusList);
			}
			statusReport.print();
		}

	}

	public synchronized void addStatus(Status status) {
		statusList.add(status);
	}

}
