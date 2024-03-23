package com.github.freeacs.dbi.domain;

import lombok.Data;

@Data
public class JobFlag {

  private JobType type;
  private final JobServiceWindow serviceWindow;

  public JobFlag(String flagStr) {
    String typeStr = flagStr.split("\\|")[0];
    type = JobType.fromString(typeStr);
    serviceWindow = JobServiceWindow.valueOf(flagStr.split("\\|")[1]);
  }

  public JobFlag(JobType jobType, JobServiceWindow jobServiceWindow) {
    type = jobType;
    serviceWindow = jobServiceWindow;
  }

  public String getFlag() {
    return type.name() + "|" + serviceWindow.name();
  }
}
