package com.github.freeacs.web.app.page.trigger;

import lombok.Getter;

@Getter
public class NotifyType {
  private Integer id;
  private String name;

  public NotifyType(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }
}
