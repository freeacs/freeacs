package com.github.freeacs.base;

import com.github.freeacs.dbi.Job;

import java.util.LinkedList;


public class DownloadLogic {

	private static LinkedList<Long> downloadList = new LinkedList<Long>();

	public static void add() {
		downloadList.add(System.currentTimeMillis());
		Log.debug(DownloadLogic.class, "Download counter increased (size: " + size() + ")");
	}

	public static void removeOldest() {
		try {
			downloadList.remove();
			Log.debug(DownloadLogic.class, "Download counter reduced (size: " + size() + ") - because of a download was completed");
		} catch (Throwable t) {

		}
	}

	public static void removeOlderThan(long maxTimeout) {
		try {
			long now = System.currentTimeMillis();
			long tms = downloadList.getFirst();
			if (now - tms > maxTimeout)
				downloadList.removeFirst();
			Log.debug(DownloadLogic.class, "Download counter reduced (size: " + size() + ") - because of timeout");
		} catch (Throwable t) {

		}
	}

	public static int size() {
		return downloadList.size();
	}

	public static boolean downloadAllowed(Job job, int downloadLimit) {
		int timeout = 10 * 60 * 1000; // 10 min
		if (job != null)
			timeout = job.getUnconfirmedTimeout() * 1000;
		DownloadLogic.removeOlderThan(timeout); // remove old downloads
		if (DownloadLogic.size() >= downloadLimit) {
			Log.warn(DownloadLogic.class, "Download cannot be run since number of concurrent downloads are above " + downloadLimit + ", download will be postponed");
			return false;
		}
		return true;
	}

}
