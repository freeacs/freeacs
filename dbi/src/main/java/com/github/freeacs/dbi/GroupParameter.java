package com.github.freeacs.dbi;

public class GroupParameter {
  private Integer id;

  private Group group;

  private Parameter parameter;

  public GroupParameter(Parameter parameter, Group group) {
    this.group = group;
    this.parameter = parameter;
  }

  @Override
  public String toString() {
    return "GP: " + parameter + " [" + group.getName() + "]";
  }

  protected void setGroup(Group group) {
    this.group = group;
  }

  public Group getGroup() {
    return group;
  }

  protected void setId(Integer i) {
    this.id = i;
  }

  public Integer getId() {
    return id;
  }

  public Parameter getParameter() {
    return parameter;
  }

  public void setParameter(Parameter parameter) {
    this.parameter = parameter;
  }

  public String getName() {
    return parameter.getUnittypeParameter().getName() + "#" + id;
  }
}
