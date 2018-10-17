package com.github.freeacs.syslogserver;

import com.github.freeacs.common.scheduler.TaskDefaultImpl;
import java.io.File;
import org.apache.commons.io.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskSpaceCheck extends TaskDefaultImpl {
  private static long freeSpace = Long.MAX_VALUE;
  private final Properties properties;

  DiskSpaceCheck(String taskName, Properties properties) {
    super(taskName);
    this.properties = properties;
  }

  private int getMinFreeDiskSpace() {
    return properties.getMinFreeDiskSpace() * 1024;
  }

  private static Logger logger = LoggerFactory.getLogger(DiskSpaceCheck.class);

  @Override
  public void runImpl() throws Throwable {
    freeSpace = FileSystemUtils.freeSpaceKb(new File(".").getAbsolutePath());
    if (freeSpace < getMinFreeDiskSpace()) {
      logger.error("Server will pause, since free disk space is " + freeSpace / 1024 + " MB.");
      SyslogServer.pause(true);
      Syslog2DB.pause(true);
    } else if (SyslogServer.isPause() && freeSpace >= getMinFreeDiskSpace()) {
      logger.info("Server will resume operation, free disk space is " + freeSpace / 1024 + " MB.");
      SyslogServer.pause(false);
      Syslog2DB.pause(false);
    }
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  static long getFreeSpace() {
    while (freeSpace == Long.MAX_VALUE) {
      try {
        Thread.sleep(
            100); // may sleep for a short while until disk space has been calculated the first time
        // after server start
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return freeSpace / 1024;
  }
}
