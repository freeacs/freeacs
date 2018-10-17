package com.github.freeacs.shell.testperf;

public class Country {
  private int percent;
  private String name;
  private City[] cities;
  private int incLowerLimit;
  private int excUpperLimit;

  public Country(String countryLine) {
    this.name = countryLine.split(":")[0].split("\\s+")[0];
    this.percent = Integer.parseInt(countryLine.split(":")[0].split("\\s+")[1].trim());
    String[] cityArr = countryLine.split(":")[1].split(",");
    cities = new City[cityArr.length];
    for (int i = 0; i < cityArr.length; i++) {
      cities[i] = new City(cityArr[i]);
    }
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

  public City[] getCities() {
    return cities;
  }

  public void setCities(City[] cities) {
    this.cities = cities;
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
    String retVal = name + " " + incLowerLimit + "-" + (excUpperLimit - 1) + ":\n";
    for (City city : cities) {
      retVal += city + "\n";
    }
    return retVal;
  }
}
