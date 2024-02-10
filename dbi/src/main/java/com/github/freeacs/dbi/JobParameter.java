package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class JobParameter {
  private Job job;
  private String unitId;
  private Parameter parameter;

  public JobParameter(Job job, String unitId, Parameter parameter) {
    this.job = job;
    this.unitId = unitId;
    this.parameter = parameter;
  }
}
