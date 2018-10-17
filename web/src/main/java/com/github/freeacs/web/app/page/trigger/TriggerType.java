package com.github.freeacs.web.app.page.trigger;

public class TriggerType {
  private Integer id;
  private String name;

  public TriggerType(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }
}
