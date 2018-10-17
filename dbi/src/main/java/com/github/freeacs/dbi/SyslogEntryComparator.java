package com.github.freeacs.dbi;

import java.util.Comparator;

public class SyslogEntryComparator implements Comparator<SyslogEntry> {
  public int compare(SyslogEntry o1, SyslogEntry o2) {
    long diff = o1.getCollectorTimestamp().getTime() - o2.getCollectorTimestamp().getTime();

    if (diff == 0) {
      if (o1.getId().intValue() > o2.getId()) {
        return -1;
      } else {
        return 1;
      }
    } else if (diff > 0) {
      return -1;
    } else {
      return 1;
    }
  }
}
