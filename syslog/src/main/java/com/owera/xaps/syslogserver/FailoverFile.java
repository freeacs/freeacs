package com.owera.xaps.syslogserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.owera.common.log.Logger;

public class FailoverFile {

	private static String LOG_FILE_NAME = "fusion-syslog-failover.log";

	private static String PROCESSING_FILE_NAME = "fusion-syslog-failover-processing.log";

	private static BufferedWriter bw = null;

	private static File f = new File(LOG_FILE_NAME);

	private static int failoverCount = 0;

	private static Logger logger = new Logger(FailoverFile.class);

	private static FailoverFile instance = new FailoverFile();

	//	private static int noWriteMs = 0;

	//	private static long tmsSinceLastWrite = System.currentTimeMillis();

	private static long created = System.currentTimeMillis();

	private static long lastFlushed = System.currentTimeMillis();

	static {
		created -= Properties.getFailoverProcessInterval() * 60 * 1000;
	}

	public static FailoverFile getInstance() {
		return instance;
	}

	public long createdTms() {
		return created;
	}

	public File getFile() {
		return f;
	}

	public synchronized void write(String s) {
		try {
			long tms = System.currentTimeMillis();
			//			long diff = tms - tmsSinceLastWrite;
			//			noWriteMs += diff;
			getBufferedWriter().write(s);
			failoverCount++;
			//			tmsSinceLastWrite = tms;
			if (tms > lastFlushed + 60000) {
				getBufferedWriter().flush();
				lastFlushed = tms;
			}
		} catch (IOException ioe) {
			logger.error("Tried to log to failover-file, but an exception ocurred: " + ioe);
		}

	}

	private BufferedWriter getBufferedWriter() throws IOException {
		if (bw == null)
			bw = new BufferedWriter(new FileWriter(f, true));
		return bw;
	}

	public static long getFailoverCount(boolean checkStorage) {
		if (checkStorage && f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				int failoverFileLineCount = 0;
				while (br.readLine() != null) {
					failoverFileLineCount++;
				}
				br.close();
				failoverCount = failoverFileLineCount;
				if (failoverFileLineCount > 0)
					logger.info("Counted " + failoverFileLineCount + " messages from failover-file.");
			} catch (IOException ioe) {
				logger.error("Tried to count lines in failover-file, but an exception occured: " + ioe);
			}
		}
		return failoverCount;
	}

	//	public synchronized static long[] resetCount() {
	//		long tmpFC = failoverCount;
	//		long tmpNWS = noWriteMs;
	//		tmsSinceLastWrite = System.currentTimeMillis();
	//		failoverCount = 0;
	//		noWriteMs = 0;
	//		return new long[] { tmpFC, tmpNWS };
	//	}

	public synchronized File rotate() throws IOException {
		try {
			created = System.currentTimeMillis();
			File tmp = f;
			BufferedWriter oldBw = getBufferedWriter();
			oldBw.close();
			oldBw = null;
			boolean success = new File(PROCESSING_FILE_NAME).delete();
			logger.info("The result of the deletion of file " + PROCESSING_FILE_NAME + " was " + success);
			success = tmp.renameTo(new File(PROCESSING_FILE_NAME));
			logger.info("The result of the rename of tmp to " + PROCESSING_FILE_NAME + " was " + success);
			failoverCount = 0;
			return new File(PROCESSING_FILE_NAME);
		} catch (IOException ioe) {
			logger.error("The rotate operation could not be performed");
			return null;
		} finally {
			f = new File(LOG_FILE_NAME);
			bw = new BufferedWriter(new FileWriter(f));
			logger.info("Created a new file for logging failover messages");
		}
	}
}
