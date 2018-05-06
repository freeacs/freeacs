package com.owera.tr069client.monitor;

import java.util.ArrayList;
import java.util.List;

import com.owera.tr069client.Session;
import com.owera.tr069client.TestCenter;
import com.owera.tr069client.messages.Download;

public class StatusReport {
	private int[] currentlyServing;

	private int[] currentlyError;

	private int[] currentlyRetrying;

	private IntExt servedOK = new IntExt();

	private IntExt servedWithFault = new IntExt();

	private int totServedOK;

	private int totServedFailed;

	private long startTms = System.currentTimeMillis();

	private IntExt retries = new IntExt();

	private IntExt retrySleep = new IntExt();

	private LongExt tms = new LongExt();

	private boolean printedHeader = false;

	public StatusReport() {
		tms.addCurrent(System.currentTimeMillis());
	}

	public List<Status> update(List<Status> statusList) {
		currentlyServing = new int[13];
		currentlyError = new int[13];
		currentlyRetrying = new int[13];
		servedOK.backup();
		servedWithFault.backup();
		retries.backup();
		retrySleep.backup();
		tms.backup();
		tms.addCurrent(System.currentTimeMillis());
		List<Status> newStatusList = new ArrayList<Status>();
		for (int i = 0; i < statusList.size(); i++) {
			Status status = statusList.get(i);
			int[] retryOccured = status.getRetryOccuredArr();
			int retriesCount = 0;
			for (int j = 0; j < retryOccured.length; j++) {
				currentlyRetrying[j] += retryOccured[j];
				retriesCount += retryOccured[j];
			}
			retries.addCurrent(retriesCount);
			retrySleep.addCurrent(status.getRetrySleep());
			//status.setRetryOccuredArr(new int[11]);
		}
		for (int i = 0; i < statusList.size(); i++) {
			Status status = statusList.get(i);
			if (status.getCurrentOperation() == Status.FIN) {
				currentlyServing[status.getCurrentOperation()]++;
				currentlyError[status.getErrorOcurred()]++;
				servedOK.addCurrent(status.getServedOK());
				servedWithFault.addCurrent(status.getServedFailed());
			} else {
				newStatusList.add(status);
			}
		}
		totServedOK += servedOK.getCurrent();
		totServedFailed += servedWithFault.getCurrent();
		return newStatusList;
	}

	public void print() {
		if (!printedHeader) {
			printedHeader = true;
			System.out.println("                SUMMARY OF THE TR-069 TEST-CLIENT RUN\n");
			System.out.println("\nShows the TOTAL summary of the whole run, the last period (10 seconds usually), and high-");
			System.out.println("lights the points in the communication where the client needs to retry to get access to");
			System.out.println("the server. This retry-mechanism is only used when you use the --powerout option. Speed is");
			System.out.println("measured in numberOfOK/minute. The Threads shows the number of threads running in the client");
			System.out.println("simulating a CPE. \n\n");
			System.out.println("TIME(s)## THREADS ##          TOTAL           ##      LAST PERIOD       ##");
			System.out.println("       ##         ##   OK   | FAILED | SPEED  ## OK |FAIL|RETRY| SPEED  ##");
		}
		float totsec = (float) (System.currentTimeMillis() - startTms) / 1000;
		int totsecInt = (int) totsec;
		if ((int) (totsec + 0.5) > totsecInt) {
			totsecInt++;
		}
		int threads = 0;
		for (int i = 0; i < TestCenter.sessionList.size(); i++) {
			Session session = TestCenter.sessionList.get(i);
			if (!session.isNotYetStarted() && !session.isStopped())
				threads++;
		}
		System.out.print(String.format("%6d ## ", totsecInt));
		System.out.print(String.format("%7d ## ", threads));
		System.out.print(String.format("%6d |", totServedOK));
		System.out.print(String.format("%7d |", totServedFailed));
		float totmin = (float) (System.currentTimeMillis() - startTms) / 60000;
		float totspeed = (float) totServedOK / totmin;
		System.out.print(String.format("%7.1f ##", totspeed));

		System.out.print(String.format("%4d|", servedOK.getCurrent()));
		System.out.print(String.format("%4d|", servedWithFault.getCurrent()));
		System.out.print(String.format("%5d|", retries.getCurrent()));
		float min = (float) (tms.getDiff()) / 60000;
		float speed = (float) servedOK.getCurrent() / min;
		System.out.print(String.format("%7.1f ## ", speed));

		for (int i = 0; i < currentlyServing.length; i++) {
			if (currentlyRetrying[i] > 0) {
				String name = Status.names[i];
				System.out.print(name + ":" + currentlyRetrying[i] + ", ");
			}
		}
		System.out.print("Downloads:" + Download.counter);
//		System.out.print(", NumberOfUnits:" + SerialNumberFactory.map.size());
//		System.out.print(", PutCounter:" + SerialNumberFactory.putCounter);
//		System.out.print(" LastTms: " + String.format("%1$tF %1$tR", SerialNumberFactory.lastTms));
		System.out.print("\n");
	}
}
