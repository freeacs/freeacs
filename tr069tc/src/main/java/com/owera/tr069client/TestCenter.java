package com.owera.tr069client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.owera.tr069client.monitor.VerboseOutputThread;

public class TestCenter implements Runnable {

	private Random random = new Random();
	private Arguments args = null;
	private long start = System.currentTimeMillis();
	private long end = 0;
	private long stepLength = 0;
	public static List<Session> sessionList;
	private static VerboseOutputThread verboseOutput = new VerboseOutputThread();
	static {
		Thread verboseThread = new Thread(verboseOutput);
		verboseThread.start();
	}

	public TestCenter(Arguments args) {
		this.args = args;
		stepLength = args.getMinutesToRunPrStep() * 60000;
		end = start + stepLength * args.getNumberOfSteps();
	}

	public void run() {

		sessionList = new ArrayList<Session>();
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "error");
		while (System.currentTimeMillis() < end) {
			for (int i = 0; i < args.getNumberOfThreadsPrStep(); i++) {
				sessionList.add(new Session(args));
			}
			for (int i = 0; i < sessionList.size(); i++) {
				Session session = sessionList.get(i);
				if (session.isNotYetStarted()) {
					// sleep extra to let the first thread init
					// resources
					try {
						if (i == 1)
							Thread.sleep(1500);
						else
							Thread.sleep(random.nextInt(100) + 25);
					} catch (InterruptedException e) {
					}
					Thread t = new Thread(sessionList.get(i));
					t.start();
				}
			}
			try {
				Thread.sleep(stepLength);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		stopTest();
	}

	private void stopTest() {
		for (int i = 0; i < sessionList.size(); i++) {
			Session session = sessionList.get(i);
			session.setStop(true);
		}
		boolean allStopped = false;
		while (!allStopped) {
			for (int i = 0; i < sessionList.size(); i++) {
				Session session = sessionList.get(i);
				if (!session.isStopped()) {
					allStopped = false;
					break;
				}
				allStopped = true;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	public static VerboseOutputThread getVerboseOutput() {
		return verboseOutput;
	}

}
