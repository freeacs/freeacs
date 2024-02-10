package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class UnitParameter {
  private final String unitId;

  private final Profile profile;

  private Parameter parameter;

  public UnitParameter(
      UnittypeParameter unittypeParameter, String unitId, String value, Profile profile) {
    this.parameter = new Parameter(unittypeParameter, value);
    this.unitId = unitId;
    this.profile = profile;
  }

  public UnitParameter(Parameter parameter, String unitId, Profile profile) {
    this.parameter = parameter;
    this.unitId = unitId;
    this.profile = profile;
  }

  public String getValue() {
    if (parameter != null) {
      return parameter.getValue();
    }
    return null;
  }

  public void setValue(String value) {
    if (parameter != null) {
      parameter.setValue(value);
    }
  }
}
