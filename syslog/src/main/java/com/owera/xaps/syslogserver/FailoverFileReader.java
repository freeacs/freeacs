package com.owera.xaps.syslogserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.owera.common.log.Logger;
import com.owera.common.util.Sleep;

public class FailoverFileReader implements Runnable {

	public static class FailoverCounter {

		private int recycled;
		private int tooOld;

		public FailoverCounter() {

		}

		public FailoverCounter(int recycled, int tooOld) {
			super();
			this.recycled = recycled;
			this.tooOld = tooOld;
		}

		public synchronized void incRecycled() {
			recycled++;
		}

		public synchronized void incTooOld() {
			tooOld++;
		}


		public synchronized FailoverCounter resetCounters() {
			FailoverCounter c = new FailoverCounter(recycled, tooOld);
			this.recycled = 0;
			this.tooOld = 0;
			return c;
		}

		public int getRecycled() {
			return recycled;
		}

		public int getTooOld() {
			return tooOld;
		}
	}

	private static FailoverCounter counter = new FailoverCounter();

	private static Logger logger = new Logger(FailoverFileReader.class);

	private static Logger failedMessages = new Logger("FAILED");

	private long MAX_AGE = Properties.getMaxFailoverMessageAge() * 60 * 60 * 1000;

	private long PROCESS_INTERVAL = Properties.getFailoverProcessInterval() * 60 * 1000;

	//	private static boolean stop;

	private static boolean ok = true;

	private static Throwable throwable;

	private static FailoverFile ff = FailoverFile.getInstance();

	//	private static int recycledCounter = 0;

	//	public static void stop() {
	//		stop = true;
	//	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				if (Sleep.isTerminated())
					return;
				if ((System.currentTimeMillis() - ff.createdTms()) > PROCESS_INTERVAL && FailoverFile.getFailoverCount(true) > 0) {
					logger.notice("Will process failover messages - that is to add them back into the system");
					File processFile = ff.rotate();
					if (processFile != null) {
						logger.notice("The failover message log file is renamed and will be processed");
						FileReader fr = new FileReader(processFile);
						BufferedReader br = new BufferedReader(fr);
						int lineCounter = 0;
						while (true) {
							if (Sleep.isTerminated())
								break;
							//							if (SyslogPackets.isHighReceiveLoad()) {
							//								Thread.sleep(1000);
							//								continue;
							//							} else {
							int size = SyslogPackets.packets.size();
							if (size > 0)
								Thread.sleep(SyslogPackets.packets.size() / 10 + 1);
							//							}
							String line = br.readLine();
							if (line == null)
								break;
							lineCounter++;
							String[] args = line.split("#@@#");
							long tms = Long.parseLong(args[1]);
							SyslogPacket packet = new SyslogPacket(args[3], args[2], tms, true);
							if ((System.currentTimeMillis() - tms) > MAX_AGE) {
								failedMessages.error(packet + "Message was too old to retry");
								counter.incTooOld();
								continue;
							}
							SyslogPackets.add(packet);
							counter.incRecycled();
							//							incRecycledCounter();
						}
						br.close();
						fr.close();
						if (processFile.delete()) {
							logger.notice("The " + processFile.getName() + " is processed (" + lineCounter + " messages) and deleted.");
							ok = true;
						} else {
							String msg = "The " + processFile.getName() + " is processed (" + lineCounter + " messages) but not deleted! It will therefore be reprocessed!";
							throwable = new Throwable(msg);
							ok = false;
							logger.error(msg);
						}
					} else {
						String msg = "Could not rename the failover message log - no processing can occur.";
						throwable = new Throwable(msg);
						ok = false;
						logger.error(msg);
					}
				} else {
					ok = true;
				}
			} catch (Throwable t) {
				throwable = t;
				ok = false;
				if (!Sleep.isTerminated())
					logger.error("Error occured, FailoverFileReader continues", t);
			}
		}

	}

	public static boolean isOk() {
		return ok;
	}

	public static Throwable getThrowable() {
		return throwable;
	}

	public static FailoverCounter getCounter() {
		return counter;
	}
}
