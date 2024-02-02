package com.github.freeacs.web.app.page.scriptexecution;

import lombok.Getter;

@Getter
public class Enumeration {
  private String value;
  private String description;

  public Enumeration(String v, String d) {
    value = v;
    description = d;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean equals(Object o) {
    if (o instanceof Enumeration) {
      Enumeration e = (Enumeration) o;
      return getValue().equals(e.getValue()) && getDescription().equals(e.getDescription());
    }
    return false;
  }

  public int hashCode() {
    return 0;
  }
}
