package com.github.freeacs.stun;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class JobHistoryEntry {
  private static final Pattern jobHistoryPattern = Pattern.compile("^(\\d+)(:(\\d+):(\\d+))?$");

  private Integer jobId;
  private Integer repeatedCount;
  private Long lastRunTms;

  public JobHistoryEntry(String entry) {
    Matcher m = jobHistoryPattern.matcher(entry.trim());
    if (m.matches()) {
      jobId = Integer.parseInt(m.group(1));
      if (m.group(3) != null && !m.group(3).trim().isEmpty()) {
        repeatedCount = Integer.parseInt(m.group(3));
      }
      if (m.group(4) != null && !m.group(4).trim().isEmpty()) {
        lastRunTms = Long.parseLong(m.group(4));
      }
    }
  }

}
