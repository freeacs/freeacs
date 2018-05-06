package com.owera.xaps.syslogserver;

import java.io.File;

import org.apache.commons.io.FileSystemUtils;

import com.owera.common.log.Logger;
import com.owera.common.scheduler.TaskDefaultImpl;

public class DiskSpaceCheck extends TaskDefaultImpl {

	public static int MIN_FREE_DISK_SPACE = Properties.getMinFreeDiskSpace() * 1024; // In KB

	private static long freeSpace = Long.MAX_VALUE;

	public DiskSpaceCheck(String taskName) {
		super(taskName);
	}

	private Logger logger = new Logger(); // Logging of internal matters - if necessary

	@Override
	public void runImpl() throws Throwable {
		freeSpace = FileSystemUtils.freeSpaceKb(new File(".").getAbsolutePath());
		if (freeSpace < MIN_FREE_DISK_SPACE) {
			logger.error("Server will pause, since free disk space is " + freeSpace / 1024 + " MB.");
			SyslogServer.pause(true);
			Syslog2DB.pause(true);
		} else if (SyslogServer.isPause() && freeSpace >= MIN_FREE_DISK_SPACE) {
			logger.notice("Server will resume operation, free disk space is " + freeSpace / 1024 + " MB.");
			SyslogServer.pause(false);
			Syslog2DB.pause(false);
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	public static long getFreeSpace() {
		while (freeSpace == Long.MAX_VALUE) {
			try {
				Thread.sleep(100); // may sleep for a short while until disk space has been calculated the first time after server start
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return freeSpace / 1024;
	}

}
