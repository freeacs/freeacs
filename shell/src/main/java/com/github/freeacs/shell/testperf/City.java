package com.github.freeacs.shell.testperf;

public class City {
  private int percent;
  private String name;
  private int incLowerLimit;
  private int excUpperLimit;

  public City(String cityStr) {
    this.name = cityStr.trim().split("\\s+")[0].trim();
    this.percent = Integer.parseInt(cityStr.trim().split("\\s+")[1].trim());
  }

  public int getPercent() {
    return percent;
  }

  public void setPercent(int percent) {
    this.percent = percent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getIncLowerLimit() {
    return incLowerLimit;
  }

  public void setIncLowerLimit(int incLowerLimit) {
    this.incLowerLimit = incLowerLimit;
  }

  public int getExcUpperLimit() {
    return excUpperLimit;
  }

  public void setExcUpperLimit(int excUpperLimit) {
    this.excUpperLimit = excUpperLimit;
  }

  public String toString() {
    return "\t" + name + " " + incLowerLimit + "-" + (excUpperLimit - 1);
  }
}
