package com.github.freeacs.dbi;

public class JobParameter {
  private Job job;
  private String unitId;
  private Parameter parameter;

  public JobParameter(Job job, String unitId, Parameter parameter) {
    this.job = job;
    this.unitId = unitId;
    this.parameter = parameter;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public String getUnitId() {
    return unitId;
  }

  public void setUnitId(String unitId) {
    this.unitId = unitId;
  }

  public Parameter getParameter() {
    return parameter;
  }

  public void setParameter(Parameter parameter) {
    this.parameter = parameter;
  }

  public String toString() {
    return "JP: " + parameter + " [" + job.getName() + "]";
  }
}
