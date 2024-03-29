package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class Heartbeat {
  public static String MISSING_HEARTBEAT_ID = "Heartbeat missing";
  public static int MAX_TIMEOUT_HOURS = 48;

  private Unittype unittype;
  private Integer id;
  private String name;
  private Group group;
  private String expression;
  private int timeoutHours;

  private boolean validateInput = true;

  public Heartbeat() {}

  public Heartbeat(
      Unittype unittype, String name, Group group, String expression, int timeoutHours) {
    setName(name);
    setUnittype(unittype);
    setExpression(expression);
    setGroup(group);
    setTimeoutHours(timeoutHours);
  }

  public void validate() {
    setName(name);
    setUnittype(unittype);
    setExpression(expression);
    setGroup(group);
    setTimeoutHours(timeoutHours);
  }

  public void setUnittype(Unittype unittype) {
    if (unittype == null) {
      throw new IllegalArgumentException("Heartbeat unittype cannot be null");
    }
    this.unittype = unittype;
  }

  public void setName(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Heartbeat name cannot be null");
    }
    this.name = name;
  }

  public void setGroup(Group group) {
    if (validateInput && group == null) {
      throw new IllegalArgumentException("Heartbeat group cannot be null");
    }
    this.group = group;
  }

  public void setExpression(String expression) {
    if (validateInput && expression == null) {
      throw new IllegalArgumentException("Heartbeat identifier expression cannot be null");
    }
    this.expression = expression;
  }

  public void setTimeoutHours(Integer timeoutHours) {
    if (validateInput && (timeoutHours < 1 || timeoutHours > 48)) {
      throw new IllegalArgumentException(
          "The generate on absence timeout hours must be between 1 and 48 (hours)");
    }
    if (timeoutHours == null || timeoutHours < 1 || timeoutHours > 48) {
      timeoutHours = 1;
    }
    this.timeoutHours = timeoutHours;
  }
}
