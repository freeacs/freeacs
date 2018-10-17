package com.github.freeacs.stun;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobHistoryEntry {
  private static Pattern jobHistoryPattern = Pattern.compile("^(\\d+)(:(\\d+):(\\d+))?$");

  private Integer jobId;
  private Integer repeatedCount;
  private Long lastRunTms;

  public JobHistoryEntry(String entry) {
    Matcher m = jobHistoryPattern.matcher(entry.trim());
    if (m.matches()) {
      jobId = Integer.parseInt(m.group(1));
      if (m.group(3) != null && !"".equals(m.group(3).trim())) {
        repeatedCount = Integer.parseInt(m.group(3));
      }
      if (m.group(4) != null && !"".equals(m.group(4).trim())) {
        lastRunTms = Long.parseLong(m.group(4));
      }
    }
  }

  public Integer getJobId() {
    return jobId;
  }

  public Integer getRepeatedCount() {
    return repeatedCount;
  }

  public Long getLastRunTms() {
    return lastRunTms;
  }
}
