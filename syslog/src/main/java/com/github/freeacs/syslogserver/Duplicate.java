package com.github.freeacs.syslogserver;

import com.github.freeacs.dbi.SyslogEntry;

public class Duplicate {
  private final SyslogEntry entry;
  private final long timeout;
  private int count;

  public Duplicate(SyslogEntry entry, long timeout) {
    this.entry = entry;
    this.timeout = timeout;
  }

  public SyslogEntry getEntry() {
    return entry;
  }

  public long getTimeout() {
    return timeout;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void incCount() {
    this.count++;
  }
}
