package com.github.freeacs.dbi;

import lombok.Data;

import java.util.Date;

@Data
public class UnitJob {
  private String unitId;
  private Integer jobId;
  private Date startTimestamp;
  private Date endTimestamp;
  private String status;
  private boolean processed;
  /** Private Integer complete;. */
  private Integer confirmedFailed;

  private Integer unconfirmedFailed;

  public UnitJob(String unitId, Integer jobId) {
    this.unitId = unitId;
    this.jobId = jobId;
    this.startTimestamp = new Date();
  }

  public UnitJob() {}

}
