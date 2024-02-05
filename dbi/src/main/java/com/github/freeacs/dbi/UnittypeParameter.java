package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class UnittypeParameter {
  private Integer id;

  private String name;

  private String oldName;

  private UnittypeParameterFlag flag;

  private UnittypeParameterValues values;

  private final Unittype unittype;

  public UnittypeParameter(Unittype unittype, String name, UnittypeParameterFlag flag) {
    if (unittype == null) {
      throw new IllegalArgumentException(
          "The unittype cannot be null in UnittypeParameter() contstructor");
    }
    this.unittype = unittype;
    if (name == null || name.trim().isEmpty() || flag == null) {
      throw new IllegalArgumentException(
          "The unittype parameter name and flag cannot be null or empty strings");
    }
    this.name = name;
    this.flag = flag;
  }

  public void setName(String name) {
    if (!name.equals(oldName)) {
      this.oldName = this.name;
      this.name = name.trim();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof UnittypeParameter oCasted) {
        if (oCasted.getId() != null && getId() != null) {
        return oCasted.getId().equals(id);
      } else if (oCasted.getName() != null && getName() != null) {
        return oCasted.getName().equals(getName());
      }
    }
    return false;
  }

  public void setFlag(UnittypeParameterFlag flag) {
    if (flag.isReadOnly() && values != null) {
      throw new IllegalArgumentException(
          "Not allowed to change parameter to read-only because there are specified values for this parameter");
    }
    this.flag = flag;
  }

  public void setValues(UnittypeParameterValues values) {
    if (values != null && getFlag().isReadOnly()) {
      throw new IllegalArgumentException(
          "Not allowed to set enumerated values for a read-only unit type parameter");
    }
    this.values = values;
  }

  protected void setValuesFromACS(UnittypeParameterValues values) {
    this.values = values;
  }
}
