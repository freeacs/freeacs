package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class GroupParameter {
  private Integer id;

  private Group group;

  private Parameter parameter;

  public GroupParameter(Parameter parameter, Group group) {
    this.group = group;
    this.parameter = parameter;
  }

  public String getName() {
    return parameter.getUnittypeParameter().getName() + "#" + id;
  }
}
