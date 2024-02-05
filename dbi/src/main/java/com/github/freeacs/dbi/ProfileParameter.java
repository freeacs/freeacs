package com.github.freeacs.dbi;

import lombok.Data;

@Data
public class ProfileParameter {
  private Integer id;

  private final Profile profile;

  private final UnittypeParameter unittypeParameter;

  private String value;

  public ProfileParameter(Profile profile, UnittypeParameter unittypeParameter, String value) {
    this.id = unittypeParameter.getId();
    this.profile = profile;
    this.unittypeParameter = unittypeParameter;
    this.value = value;
  }

  public void setValue(String value) {
    if (value == null) {
      value = "";
    }
    this.value = value;
  }
}
