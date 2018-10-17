package com.github.freeacs.dbi;

import java.util.Date;

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

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  public Integer getJobId() {
    return jobId;
  }

  public void setJobId(Integer jobId) {
    this.jobId = jobId;
  }

  public Date getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(Date start_timestamp) {
    this.startTimestamp = start_timestamp;
  }

  public Date getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(Date end_timestamp) {
    this.endTimestamp = end_timestamp;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  @Override
  public String toString() {
    return unitId
        + " "
        + jobId
        + " "
        + status
        + " "
        + processed
        + " "
        + confirmedFailed
        + ", "
        + unconfirmedFailed;
  }

  public Integer getConfirmedFailed() {
    return confirmedFailed;
  }

  public void setConfirmedFailed(Integer confirmedFailed) {
    this.confirmedFailed = confirmedFailed;
  }

  public Integer getUnconfirmedFailed() {
    return unconfirmedFailed;
  }

  public void setUnconfirmedFailed(Integer unconfirmedFailed) {
    this.unconfirmedFailed = unconfirmedFailed;
  }
}
