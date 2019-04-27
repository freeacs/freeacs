package com.github.freeacs.tr069.base;

import com.github.freeacs.dbi.Job;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class DownloadLogic {
  private static List<Long> downloadList = new LinkedList<>();

  public static void add() {
    downloadList.add(System.currentTimeMillis());
    log.debug("Download counter increased (size: " + size() + ")");
  }

  public static void removeOldest() {
    try {
      downloadList.remove(0);
      log.debug("Download counter reduced (size: " + size() + ") - because of a download was completed");
    } catch (Throwable ignored) {
    }
  }

  private static void removeOlderThan(long maxTimeout) {
    try {
      long now = System.currentTimeMillis();
      long tms = downloadList.get(0);
      if (now - tms > maxTimeout) {
        downloadList.remove(0);
      }
      log.debug("Download counter reduced (size: " + size() + ") - because of timeout");
    } catch (Throwable ignored) {
    }
  }

  public static int size() {
    return downloadList.size();
  }

  public static boolean downloadAllowed(Job job, int downloadLimit) {
    int timeout = 10 * 60 * 1000; // 10 min
    if (job != null) {
      timeout = job.getUnconfirmedTimeout() * 1000;
    }
    removeOlderThan(timeout); // remove old downloads
    if (size() >= downloadLimit) {
      log.warn("Download cannot be run since number of concurrent downloads are above "
              + downloadLimit
              + ", download will be postponed");
      return false;
    }
    return true;
  }
}
